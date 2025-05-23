package com.pluxity.facility.line;

import com.pluxity.facility.line.dto.LineRequest;
import com.pluxity.facility.line.dto.LineResponse;
import com.pluxity.facility.station.Station;
import com.pluxity.facility.station.StationRepository;
import com.pluxity.global.exception.CustomException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LineService {

    private final LineRepository lineRepository;
    private final StationRepository stationRepository;

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
        Line line =
                lineRepository
                        .findById(id)
                        .orElseThrow(() -> notFoundException("Line not found", "해당 호선을 찾을 수 없습니다."));
        return LineResponse.from(line);
    }

    @Transactional(readOnly = true)
    public List<Long> findStationsByLineId(Long lineId) {
        Line line =
                lineRepository
                        .findById(lineId)
                        .orElseThrow(() -> notFoundException("Line not found", "해당 호선을 찾을 수 없습니다."));
        return line.getStations().stream().map(Station::getId).collect(Collectors.toList());
    }

    @Transactional
    public void update(Long id, LineRequest request) {
        Line line =
                lineRepository
                        .findById(id)
                        .orElseThrow(() -> notFoundException("Line not found", "해당 호선을 찾을 수 없습니다."));

        line.update(Line.builder().name(request.name()).color(request.color()).build());
    }

    @Transactional
    public void delete(Long id) {
        Line line =
                lineRepository
                        .findById(id)
                        .orElseThrow(() -> notFoundException("Line not found", "해당 호선을 찾을 수 없습니다."));

        List<Station> stations = new ArrayList<>(line.getStations());

        stations.forEach(station -> station.changeLine(null));

        lineRepository.delete(line);
    }

    @Transactional
    public void addStationToLine(Long lineId, Long stationId) {
        Line line =
                lineRepository
                        .findById(lineId)
                        .orElseThrow(() -> notFoundException("Line not found", "해당 호선을 찾을 수 없습니다."));

        Station station =
                stationRepository
                        .findById(stationId)
                        .orElseThrow(() -> notFoundException("Station not found", "해당 역을 찾을 수 없습니다."));

        station.changeLine(line);
    }

    private static CustomException notFoundException(String Line_not_found, String message) {
        return new CustomException(Line_not_found, HttpStatus.NOT_FOUND, message);
    }
}
