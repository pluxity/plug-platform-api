package com.pluxity.facility.station;

import com.pluxity.facility.facility.Facility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class StationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StationRepository stationRepository;

    private Station station1;
    private Long station1Id; // To store the ID after saving

    @BeforeEach
    void setUp() {
        station1 = new Station("Test Facility One", "Description for Facility One", "Route Alpha");
        // The Station constructor creates its own Facility. Set the code on that internal facility.
        // The name and description for the facility are passed via the Station constructor.
        station1.getFacility().setCode("CODE001");
        stationRepository.save(station1);
        station1Id = station1.getId(); // Capture the generated ID

        Station station2 = new Station("Test Facility Two", "Description for Facility Two", "Route Beta");
        station2.getFacility().setCode("CODE002");
        stationRepository.save(station2);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("findByCode: 코드로 스테이션 조회 시 해당 스테이션 반환")
    void findByCode_whenStationWithCodeExists_shouldReturnStation() {
        // Act
        Optional<Station> foundStationOpt = stationRepository.findByCode("CODE001");

        // Assert
        assertThat(foundStationOpt).isPresent();
        Station foundStation = foundStationOpt.get();
        assertThat(foundStation.getId()).isEqualTo(station1Id); // Compare with stored ID
        assertThat(foundStation.getFacility().getCode()).isEqualTo("CODE001");
        assertThat(foundStation.getFacility().getName()).isEqualTo("Test Facility One");
        assertThat(foundStation.getRoute()).isEqualTo("Route Alpha");
    }

    @Test
    @DisplayName("findByCode: 존재하지 않는 코드로 스테이션 조회 시 빈 Optional 반환")
    void findByCode_whenStationWithCodeDoesNotExist_shouldReturnEmpty() {
        // Act
        Optional<Station> foundStationOpt = stationRepository.findByCode("NONEXISTENT_CODE");

        // Assert
        assertThat(foundStationOpt).isNotPresent();
    }
}
