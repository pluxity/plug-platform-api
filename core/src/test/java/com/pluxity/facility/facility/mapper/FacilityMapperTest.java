package com.pluxity.facility.facility.mapper;

import com.pluxity.facility.facility.Facility;
import com.pluxity.facility.facility.dto.FacilityResponse;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FacilityMapperTest {

    private final FacilityMapper facilityMapper = FacilityMapper.INSTANCE;

    @Test
    void toFacilityResponse_shouldMapCorrectly() {
        // Arrange
        Facility facility = new Facility("Test Facility Name", "Test Facility Desc", "History Comment");
        // Set fields that are normally set by JPA or specific methods
        facility.setId(1L);
        facility.setCode("F001"); // Code is set via constructor or updateCode
        facility.updateDrawingFileId(10L);
        facility.updateThumbnailFileId(20L);
        // Note: FacilityCategory not set here, assumed nullable or not part of simple FacilityResponse mapping

        // Act
        FacilityResponse response = facilityMapper.toFacilityResponse(facility);

        // Assert
        assertNotNull(response);
        assertEquals(facility.getId(), response.id());
        assertEquals(facility.getName(), response.name());
        assertEquals(facility.getCode(), response.code());
        assertEquals(facility.getDescription(), response.description());
        assertEquals(facility.getDrawingFileId(), response.drawingFileId());
        assertEquals(facility.getThumbnailFileId(), response.thumbnailFileId());
    }

    @Test
    void toFacilityResponse_whenFacilityIsNull_shouldReturnNull() {
        assertNull(facilityMapper.toFacilityResponse(null));
    }
}
