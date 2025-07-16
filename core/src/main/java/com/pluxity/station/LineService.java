package com.pluxity.station;

import static com.pluxity.global.constant.ErrorCode.DUPLICATE_LINE_NAME;
import static com.pluxity.global.constant.ErrorCode.NOT_FOUND_LINE;

import com.pluxity.global.exception.CustomException;
import com.pluxity.global.utils.SortUtils;
import com.pluxity.station.dto.LineCreateRequest;
import com.pluxity.station.dto.LineResponse;
import com.pluxity.station.dto.LineUpdateRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
    public Long save(LineCreateRequest request) {
        lineRepository
                .findByName(request.name())
                .ifPresent(
                        line -> {
                            throw new CustomException(DUPLICATE_LINE_NAME, request.name());
                        });
        Line line = Line.builder().name(request.name()).color(request.color()).build();
        return lineRepository.save(line).getId();
    }

    @Transactional(readOnly = true)
    public List<LineResponse> findAll() {
        return lineRepository.findAll(SortUtils.getOrderByCreatedAtDesc()).stream()
                .map(LineResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public LineResponse findById(Long id) {
        Line line = lineRepository.findById(id).orElseThrow(() -> notFoundException(id));
        return LineResponse.from(line);
    }

    @Transactional(readOnly = true)
    public Line findLineById(Long id) {
        return lineRepository.findById(id).orElseThrow(() -> notFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Long> findStationsByLineId(Long lineId) {
        Line line = findLineById(lineId);
        return line.getStations().stream().map(Station::getId).collect(Collectors.toList());
    }

    @Transactional
    public void update(Long id, LineUpdateRequest request) {
        Line line = findLineById(id);
        line.update(Line.builder().name(request.name()).color(request.color()).build());
    }

    @Transactional
    public void delete(Long id) {
        Line line = findLineById(id);

        List<Station> stations = new ArrayList<>(line.getStations());
        for (Station station : stations) {
            station.removeLine(line);
        }

        lineRepository.delete(line);
    }

    @Transactional
    public void addStationToLine(Long lineId, Long stationId) {
        Line line = findLineById(lineId);
        Station station = stationService.findStationById(stationId);
        station.addLine(line);
    }

    private static CustomException notFoundException(Long id) {
        return new CustomException(NOT_FOUND_LINE, id);
    }
}
