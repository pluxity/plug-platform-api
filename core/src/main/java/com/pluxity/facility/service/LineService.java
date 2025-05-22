package com.pluxity.facility.service;

import com.pluxity.facility.dto.LineRequest;
import com.pluxity.facility.dto.LineResponse;
import com.pluxity.facility.entity.Line;
import com.pluxity.facility.entity.Station;
import com.pluxity.facility.repository.LineRepository;
import com.pluxity.facility.repository.StationRepository;
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
        Line line = Line.create(request.name(), request.color(), request.route());
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
                        .orElseThrow(
                                () ->
                                        new CustomException(
                                                "Line not found", HttpStatus.NOT_FOUND, "해당 호선을 찾을 수 없습니다."));
        return LineResponse.from(line);
    }

    @Transactional(readOnly = true)
    public List<Long> findStationsByLineId(Long lineId) {
        Line line =
                lineRepository
                        .findById(lineId)
                        .orElseThrow(
                                () ->
                                        new CustomException(
                                                "Line not found", HttpStatus.NOT_FOUND, "해당 호선을 찾을 수 없습니다."));
        return line.getStations().stream().map(Station::getId).collect(Collectors.toList());
    }

    @Transactional
    public void update(Long id, LineRequest request) {
        Line line =
                lineRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new CustomException(
                                                "Line not found", HttpStatus.NOT_FOUND, "해당 호선을 찾을 수 없습니다."));

        line.update(Line.create(request.name(), request.color(), request.route()));
    }

    @Transactional
    public void delete(Long id) {
        Line line =
                lineRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new CustomException(
                                                "Line not found", HttpStatus.NOT_FOUND, "해당 호선을 찾을 수 없습니다."));

        List<Station> stations = new ArrayList<>(line.getStations());

        stations.forEach(station -> station.changeLine(null));

        lineRepository.delete(line);
    }

    @Transactional
    public void addStationToLine(Long lineId, Long stationId) {
        Line line =
                lineRepository
                        .findById(lineId)
                        .orElseThrow(
                                () ->
                                        new CustomException(
                                                "Line not found", HttpStatus.NOT_FOUND, "해당 호선을 찾을 수 없습니다."));

        Station station =
                stationRepository
                        .findById(stationId)
                        .orElseThrow(
                                () ->
                                        new CustomException(
                                                "Station not found", HttpStatus.NOT_FOUND, "해당 역을 찾을 수 없습니다."));

        station.changeLine(line);
    }
}
