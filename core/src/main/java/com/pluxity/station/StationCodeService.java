package com.pluxity.station;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StationCodeService {

    private final StationCodeRepository stationCodeRepository;

    @Transactional
    public void save(Long stationId, String code) {
        stationCodeRepository.save(StationCode.builder().stationId(stationId).code(code).build());
    }

    @Transactional(readOnly = true)
    public List<String> findCodesByStationId(Long stationId) {
        return stationCodeRepository.findByStationIdOrderByCreatedAtDesc(stationId).stream()
                .map(StationCode::getCode)
                .toList();
    }

    @Transactional
    public void deleteByStationId(Long stationId) {
        stationCodeRepository.deleteByStationId(stationId);
    }
}
