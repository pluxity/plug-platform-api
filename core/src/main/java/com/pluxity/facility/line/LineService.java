package com.pluxity.facility.line;

import com.pluxity.facility.line.dto.LineRequest;
import com.pluxity.facility.line.dto.LineResponse;
import com.pluxity.facility.station.Station;
import com.pluxity.facility.station.StationService;
import com.pluxity.global.exception.CustomException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LineService {

    private final LineRepository lineRepository;
    private StationService stationService;

    @Autowired
    public void setStationService(@Lazy StationService stationService) {
        this.stationService = stationService;
    }

    public LineService(LineRepository lineRepository) {
        this.lineRepository = lineRepository;
    }

    @Transactional
    public Long save(LineRequest request) {
        Line line = Line.builder().name(request.name()).color(request.color()).build();
        return lineRepository.save(line).getId();
    }

    @Transactional(readOnly = true)
    public List<LineResponse> findAll() {
        return lineRepository.findAll().stream().map(LineResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public LineResponse findById(Long id) {
        Line line = lineRepository.findById(id).orElseThrow(LineService::notFoundException);
        return LineResponse.from(line);
    }

    @Transactional(readOnly = true)
    public Line findLineById(Long id) {
        return lineRepository.findById(id).orElseThrow(LineService::notFoundException);
    }

    @Transactional(readOnly = true)
    public List<Long> findStationsByLineId(Long lineId) {
        Line line = findLineById(lineId);
        return line.getStations().stream().map(Station::getId).collect(Collectors.toList());
    }

    @Transactional
    public void update(Long id, LineRequest request) {
        Line line = findLineById(id);
        line.update(Line.builder().name(request.name()).color(request.color()).build());
    }

    @Transactional
    public void delete(Long id) {
        Line line = findLineById(id);
        List<Station> stations = new ArrayList<>(line.getStations());
        stations.forEach(station -> station.changeLine(null));
        lineRepository.delete(line);
    }

    @Transactional
    public void addStationToLine(Long lineId, Long stationId) {
        Line line = findLineById(lineId);
        Station station = stationService.findStationById(stationId);
        station.changeLine(line);
    }

    private static CustomException notFoundException() {
        return new CustomException("Line not found", HttpStatus.NOT_FOUND, "해당 호선을 찾을 수 없습니다.");
    }
}
