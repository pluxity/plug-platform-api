package com.pluxity.facility.building;

// import com.pluxity.facility.facility.Facility; // Removed as not strictly needed
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BuildingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BuildingRepository buildingRepository;

    @Test
    @DisplayName("Building 저장 및 ID로 조회 시 정상적으로 동작한다")
    void saveAndFindById_shouldWorkCorrectly() {
        // Arrange
        // Building constructor (name, description) creates its Facility
        // Facility name and description are passed from Building constructor
        Building building = new Building("Tech Park Building", "Main R&D Building");
        // Set the code for the composed facility
        building.getFacility().updateCode("TP001");

        // Act
        Building savedBuilding = buildingRepository.save(building);
        entityManager.flush(); // Ensure data is written to DB
        entityManager.clear(); // Detach entities to ensure fresh load from DB

        Optional<Building> foundBuildingOpt = buildingRepository.findById(savedBuilding.getId());

        // Assert
        assertThat(foundBuildingOpt).isPresent();
        Building foundBuilding = foundBuildingOpt.get();
        assertThat(foundBuilding.getId()).isEqualTo(savedBuilding.getId());

        assertThat(foundBuilding.getFacility()).isNotNull();
        assertThat(foundBuilding.getFacility().getName()).isEqualTo("Tech Park Building");
        assertThat(foundBuilding.getFacility().getCode()).isEqualTo("TP001");
        assertThat(foundBuilding.getFacility().getDescription()).isEqualTo("Main R&D Building");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 Building 조회 시 빈 Optional을 반환한다")
    void findById_withNonExistentId_shouldReturnEmptyOptional() {
        // Act
        Optional<Building> foundBuildingOpt = buildingRepository.findById(999L); // Assuming 999L does not exist

        // Assert
        assertThat(foundBuildingOpt).isNotPresent();
    }
}
