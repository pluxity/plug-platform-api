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
public class StationCodeService {

    private final StationCodeRepository stationCodeRepository;

    @Transactional
    public void save(Station station, String code) {
        stationCodeRepository.save(StationCode.builder().station(station).code(code).build());
    }

    @Transactional(readOnly = true)
    public List<String> findCodesByStation(Station station) {
        return stationCodeRepository.findByStationOrderByCreatedAtDesc(station).stream()
                .map(StationCode::getCode)
                .toList();
    }

    @Transactional
    public void deleteByStation(Station station) {
        stationCodeRepository.deleteByStation(station);
    }

    @Transactional(readOnly = true)
    public Map<Station, List<String>> findCodeMapByStationIds(List<Station> stations) {
        if (CollectionUtils.isEmpty(stations)) {
            return Collections.emptyMap();
        }
        return stationCodeRepository.findByStationInOrderByCreatedAtDesc(stations).stream()
                .collect(
                        Collectors.groupingBy(
                                StationCode::getStation,
                                Collectors.mapping(StationCode::getCode, Collectors.toList())));
    }
}
