package com.pluxity.domains.sasang.station;

// Entities from core module are not directly used here, but their fields are tested via composed objects
// import com.pluxity.facility.facility.Facility;
// import com.pluxity.facility.station.Station;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SasangStationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SasangStationRepository sasangStationRepository;

    private SasangStation sStation1;
    private Long sStation1Id;
    private final String sStation1FacilityCode = "SASANG_F001";
    private final String sStation1FacilityName = "Sasang Facility One"; // Name for the internal Facility
    private final String sStation1FacilityDesc = "Description for Sasang Station One"; // Desc for internal Facility
    private final String sStation1Route = "Route Sasang Alpha"; // Route for internal Station
    private final String sStation1ExternalCode = "S_EXT001";


    @BeforeEach
    void setUp() {
        sStation1 = new SasangStation(sStation1FacilityName, sStation1FacilityDesc, sStation1Route, sStation1ExternalCode);
        // Set the code on the facility composed within the station, which is composed within SasangStation
        sStation1.getStation().getFacility().setCode(sStation1FacilityCode);
        // Name and Description for Facility are passed via SasangStation -> Station constructor.

        sasangStationRepository.save(sStation1);
        sStation1Id = sStation1.getId();

        SasangStation sStation2 = new SasangStation("Sasang Facility Two", "Desc Two", "Route Sasang Beta", "S_EXT002");
        sStation2.getStation().getFacility().setCode("SASANG_F002");
        // sStation2.getStation().getFacility().setName("Sasang Facility Two"); // Already set by constructor
        sasangStationRepository.save(sStation2);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("findByCode: 시설물 코드로 사상역 조회 시 해당 사상역 반환")
    void findByCode_whenStationWithFacilityCodeExists_shouldReturnStation() {
        Optional<SasangStation> foundOpt = sasangStationRepository.findByCode(sStation1FacilityCode);
        assertThat(foundOpt).isPresent();
        SasangStation found = foundOpt.get();
        assertThat(found.getId()).isEqualTo(sStation1Id);
        assertThat(found.getStation().getFacility().getCode()).isEqualTo(sStation1FacilityCode);
        assertThat(found.getStation().getFacility().getName()).isEqualTo(sStation1FacilityName);
        assertThat(found.getExternalCode()).isEqualTo(sStation1ExternalCode);
    }

    @Test
    @DisplayName("findByCode: 존재하지 않는 시설물 코드로 조회 시 빈 Optional 반환")
    void findByCode_whenStationWithFacilityCodeDoesNotExist_shouldReturnEmpty() {
        assertThat(sasangStationRepository.findByCode("NONEXISTENT_CODE")).isNotPresent();
    }

    @Test
    @DisplayName("findByName: 시설물 이름으로 사상역 조회 시 해당 사상역 반환")
    void findByName_whenStationWithFacilityNameExists_shouldReturnStation() {
        Optional<SasangStation> foundOpt = sasangStationRepository.findByName(sStation1FacilityName);
        assertThat(foundOpt).isPresent();
        SasangStation found = foundOpt.get();
        assertThat(found.getId()).isEqualTo(sStation1Id);
        assertThat(found.getStation().getFacility().getName()).isEqualTo(sStation1FacilityName);
        assertThat(found.getStation().getFacility().getCode()).isEqualTo(sStation1FacilityCode);
    }

    @Test
    @DisplayName("findByName: 존재하지 않는 시설물 이름으로 조회 시 빈 Optional 반환")
    void findByName_whenStationWithFacilityNameDoesNotExist_shouldReturnEmpty() {
        assertThat(sasangStationRepository.findByName("Nonexistent Facility Name")).isNotPresent();
    }

    @Test
    @DisplayName("findByNameAndIdNot: 다른 ID를 가진 동일 시설물 이름의 사상역 조회")
    void findByNameAndIdNot_whenNameExistsAndIdIsDifferent_shouldReturnStation() {
        SasangStation sStationSameName = new SasangStation(sStation1FacilityName, "Desc Same Name", "Route Gamma", "S_EXT003");
        sStationSameName.getStation().getFacility().setCode("SASANG_F003"); // Different code
        sasangStationRepository.save(sStationSameName);
        entityManager.flush();
        entityManager.clear();

        Optional<SasangStation> foundOpt = sasangStationRepository.findByNameAndIdNot(sStation1FacilityName, sStationSameName.getId());
        assertThat(foundOpt).isPresent();
        assertThat(foundOpt.get().getId()).isEqualTo(sStation1Id);
    }

    @Test
    @DisplayName("findByNameAndIdNot: 동일 ID를 가진 동일 시설물 이름의 사상역 조회 시 빈 Optional 반환")
    void findByNameAndIdNot_whenNameExistsAndIdIsSame_shouldReturnEmpty() {
        Optional<SasangStation> foundOpt = sasangStationRepository.findByNameAndIdNot(sStation1FacilityName, sStation1Id);
        assertThat(foundOpt).isNotPresent();
    }

    @Test
    @DisplayName("findByCodeAndIdNot: 다른 ID를 가진 동일 시설물 코드의 사상역 조회")
    void findByCodeAndIdNot_whenCodeExistsAndIdIsDifferent_shouldReturnStation() {
        SasangStation sStationSameCode = new SasangStation("Another Facility Name", "Desc Same Code", "Route Delta", "S_EXT004");
        sStationSameCode.getStation().getFacility().setCode(sStation1FacilityCode); // Same code
        // sStationSameCode.getStation().getFacility().setName("Another Facility Name"); // Set by constructor
        sasangStationRepository.save(sStationSameCode);
        entityManager.flush();
        entityManager.clear();

        Optional<SasangStation> foundOpt = sasangStationRepository.findByCodeAndIdNot(sStation1FacilityCode, sStationSameCode.getId());
        assertThat(foundOpt).isPresent();
        assertThat(foundOpt.get().getId()).isEqualTo(sStation1Id);
    }

    @Test
    @DisplayName("findByCodeAndIdNot: 동일 ID를 가진 동일 시설물 코드의 사상역 조회 시 빈 Optional 반환")
    void findByCodeAndIdNot_whenCodeExistsAndIdIsSame_shouldReturnEmpty() {
        Optional<SasangStation> foundOpt = sasangStationRepository.findByCodeAndIdNot(sStation1FacilityCode, sStation1Id);
        assertThat(foundOpt).isNotPresent();
    }

    @Test
    @DisplayName("findByExternalCode: 외부 코드로 사상역 조회")
    void findByExternalCode_whenExists_shouldReturnStation() {
        Optional<SasangStation> foundOpt = sasangStationRepository.findByExternalCode(sStation1ExternalCode);
        assertThat(foundOpt).isPresent();
        assertThat(foundOpt.get().getId()).isEqualTo(sStation1Id);
        assertThat(foundOpt.get().getExternalCode()).isEqualTo(sStation1ExternalCode);
    }

    @Test
    @DisplayName("findByExternalCode: 존재하지 않는 외부 코드로 조회 시 빈 Optional 반환")
    void findByExternalCode_whenDoesNotExist_shouldReturnEmpty() {
        assertThat(sasangStationRepository.findByExternalCode("NON_EXISTENT_EXTERNAL_CODE")).isNotPresent();
    }
}
