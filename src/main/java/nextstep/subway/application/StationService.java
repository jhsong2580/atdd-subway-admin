package nextstep.subway.application;

import java.util.List;
import java.util.stream.Collectors;
import nextstep.subway.domain.LineStationRepository;
import nextstep.subway.domain.Station;
import nextstep.subway.domain.StationRepository;
import nextstep.subway.dto.request.StationRequest;
import nextstep.subway.dto.response.StationResponse;
import nextstep.subway.exception.StationNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StationService {

    private final StationRepository stationRepository;
    private final LineStationRepository lineStationRepository;

    public StationService(StationRepository stationRepository, LineStationRepository lineStationRepository) {
        this.stationRepository = stationRepository;
        this.lineStationRepository = lineStationRepository;
    }

    @Transactional
    public StationResponse saveStation(StationRequest stationRequest) {
        Station persistStation = stationRepository.save(stationRequest.toStation());
        return StationResponse.of(persistStation);
    }

    public List<StationResponse> findAllStations() {
        List<Station> stations = stationRepository.findAll();

        return stations.stream()
            .map(station -> StationResponse.of(station))
            .collect(Collectors.toList());
    }

    @Transactional
    public void deleteStationById(Long id) {
        Station station = stationRepository.findById(id)
            .orElseThrow(StationNotFoundException::new);
        lineStationRepository.deleteAllByStation(station);
        stationRepository.deleteById(id);
    }

    public StationResponse findStationById(Long id) {
        Station station = stationRepository.findById(id)
            .orElseThrow(StationNotFoundException::new);

        return StationResponse.of(station);
    }

    @Transactional
    public void updateLineById(Long id, StationRequest stationRequest) {
        Station station = stationRepository.findById(id)
            .orElseThrow(StationNotFoundException::new);
        Station stationUpdate = stationRequest.toStation();
        station.update(stationUpdate);
    }
}
