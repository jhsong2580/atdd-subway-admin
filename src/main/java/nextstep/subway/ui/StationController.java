package nextstep.subway.ui;

import java.net.URI;
import java.util.List;
import nextstep.subway.application.StationService;
import nextstep.subway.dto.request.StationRequest;
import nextstep.subway.dto.response.StationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/stations")
public class StationController {

    private final StationService stationService;

    @Autowired
    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @PostMapping()
    public ResponseEntity<StationResponse> createStation(
        @RequestBody StationRequest stationRequest) {
        StationResponse station = stationService.saveStation(stationRequest);
        return ResponseEntity.created(URI.create("/stations/" + station.getId()))
            .body(station);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<StationResponse>> showStations() {
        return ResponseEntity.ok()
            .body(stationService.findAllStations());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteStation(@PathVariable Long id) {
        stationService.deleteStationById(id);
        return ResponseEntity.noContent()
            .build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity handleIllegalArgsException() {
        return ResponseEntity.badRequest()
            .build();
    }

    @GetMapping("/{id}")
    public ResponseEntity getStationDetail(@PathVariable Long id) {
        return ResponseEntity.ok()
            .body(stationService.findStationById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity updateStationDetail(@PathVariable Long id,
        @RequestBody StationRequest stationRequest) {
        stationService.updateLineById(id, stationRequest);
        return ResponseEntity.ok()
            .build();
    }
}
