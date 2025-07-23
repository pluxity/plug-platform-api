package com.pluxity.station;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class StationLineService {

    private final StationLineRepository stationLineRepository;

    @Transactional
    public void save(Station station, Line line) {
        stationLineRepository.save(StationLine.builder().station(station).line(line).build());
    }

    @Transactional(readOnly = true)
    public List<Long> findLinesByStation(Station station) {
        return stationLineRepository.findByStationOrderByCreatedAtDesc(station).stream()
                .map(v -> v.getLine().getId())
                .toList();
    }

    @Transactional
    public void deleteByStation(Station station) {
        stationLineRepository.deleteByStation(station);
    }

    @Transactional(readOnly = true)
    public Map<Station, List<Long>> findLineMapByStationIds(List<Station> stations) {
        if (CollectionUtils.isEmpty(stations)) {
            return Collections.emptyMap();
        }
        return stationLineRepository.findByStationInOrderByCreatedAtDesc(stations).stream()
                .collect(
                        Collectors.groupingBy(
                                StationLine::getStation,
                                Collectors.mapping(v -> v.getLine().getId(), Collectors.toList())));
    }

    @Transactional(readOnly = true)
    public boolean checkAlreadyConnect(Station station, Line line) {
        return stationLineRepository.existsByStationAndLine(station, line);
    }

    @Transactional
    public void deleteStationLine(Station station, Line line) {
        stationLineRepository.deleteByStationAndLine(station, line);
    }
}
