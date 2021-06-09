package nextstep.subway.line.application;

import nextstep.subway.line.application.exception.LineDuplicatedException;
import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LineCommandServiceTest {
    private LineCommandService lineCommandService;

    @Mock
    private LineRepository lineRepository;

    @Mock
    private LineQueryUseCase lineQueryUseCase;
    private LineRequest lineRequest1;
    private LineRequest lineRequest2;
    private Line line1;

    @BeforeEach
    void setUp() {
        lineCommandService = new LineCommandService(lineRepository, lineQueryUseCase);
        lineRequest1 = new LineRequest("1호선", "blue");
        lineRequest2 = new LineRequest("2호선", "green");
        line1 = new Line("1호선", "blue");
    }

    @DisplayName("요청한 지하철 노선을 저장하고 저장된 지하철 노선을 리턴한다.")
    @Test
    void saveLine() {
        //given
        when(lineRepository.save(any(Line.class))).thenReturn(line1);

        //when
        LineResponse actual = lineCommandService.saveLine(lineRequest1);

        //then
        assertThat(actual.getName()).isEqualTo("1호선");
        assertThat(actual.getColor()).isEqualTo("blue");
    }

    @DisplayName("요청한 지하철 노선 이름이 이미 존재하면 예외를 발생시킨다.")
    @Test
    void saveLineDuplicatedException() {
        //given
        doThrow(LineDuplicatedException.class).when(lineQueryUseCase).checkDuplicatedLineName(anyString());

        //when
        assertThatThrownBy(() -> lineCommandService.saveLine(lineRequest1))
                .isInstanceOf(LineDuplicatedException.class); // then
    }

    @DisplayName("요청한 ID에 해당하는 노선을 새로운 이름과 색깔로 대체한다.")
    @Test
    void update() {
        //given
        when(lineQueryUseCase.findById(anyLong())).thenReturn(line1);

        //when
        lineCommandService.updateLine(anyLong(), lineRequest2);

        //then
        assertThat(line1.getName()).isEqualTo(lineRequest2.getName());
        assertThat(line1.getColor()).isEqualTo(lineRequest2.getColor());
    }

    @DisplayName("요청한 ID에 해당하는 노선의 대체할 이름이 이미 존재할 경우 예외를 발생시킨다.")
    @Test
    void updateDuplicatedNameException() {
        //given
        when(lineQueryUseCase.findById(anyLong())).thenReturn(line1);
        doThrow(LineDuplicatedException.class).when(lineQueryUseCase).checkDuplicatedLineName(anyString());

        //when
        assertThatThrownBy(() -> lineCommandService.updateLine(anyLong(), lineRequest2))
                .isInstanceOf(LineDuplicatedException.class); //then
    }

    @DisplayName("요청한 ID에 해당하는 노선을 삭제한다.")
    @Test
    void delete() {
        //given
        when(lineQueryUseCase.findById(anyLong())).thenReturn(line1);

        //when
        lineCommandService.deleteLine(anyLong());

        //then
        verify(lineRepository).delete(line1);
    }
}