package com.pluxity.facility.building.mapper;

import com.pluxity.facility.building.Building;
import com.pluxity.facility.building.dto.BuildingResponse;
import com.pluxity.facility.facility.Facility;
import com.pluxity.facility.facility.dto.FacilityResponse; // For assertion

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BuildingMapperTest {

    private final BuildingMapper buildingMapper = BuildingMapper.INSTANCE;

    @Test
    @DisplayName("Building 엔티티를 BuildingResponse DTO로 정확히 매핑한다")
    void toBuildingResponse_shouldMapCorrectly() {
        // Arrange
        // Building constructor creates its own Facility.
        // public Building(String name, String description)
        // this.facility = new Facility(name, null, description, "Initial comment for Building's facility");
        Building building = new Building("Test Building Name", "Test Building Description");
        building.setId(1L); // Set Building's own ID

        // Get the composed facility to set its ID and other fields for assertion
        Facility composedFacility = building.getFacility(); // This facility is created by Building's constructor
        assertNotNull(composedFacility, "Facility should be initialized by Building constructor");
        composedFacility.setId(10L); // Set Facility's ID
        composedFacility.updateCode("B001"); // Set Facility's code
        // Name and Description of composedFacility are set by Building's constructor

        // Act
        BuildingResponse response = buildingMapper.toBuildingResponse(building);

        // Assert
        assertNotNull(response);
        assertEquals(building.getId(), response.id());

        assertNotNull(response.facility(), "FacilityResponse DTO should not be null");
        FacilityResponse facilityResponse = response.facility();
        assertEquals(composedFacility.getId(), facilityResponse.id());
        assertEquals("Test Building Name", facilityResponse.name()); // Name passed via Building constructor
        assertEquals("B001", facilityResponse.code());
        assertEquals("Test Building Description", facilityResponse.description()); // Description passed via Building constructor

        // Floors are ignored by the mapper, so response.floors() should be null
        // as per @Mapping(target = "floors", ignore = true)
        assertNull(response.floors());
    }

    @Test
    @DisplayName("Building 엔티티가 null일 경우 null을 반환한다")
    void toBuildingResponse_whenBuildingIsNull_shouldReturnNull() {
        assertNull(buildingMapper.toBuildingResponse(null));
    }
}
