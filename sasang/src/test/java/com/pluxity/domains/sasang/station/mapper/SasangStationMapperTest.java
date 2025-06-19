package com.pluxity.domains.sasang.station.mapper;

import com.pluxity.domains.sasang.station.SasangStation;
import com.pluxity.domains.sasang.station.dto.SasangStationResponse;
import com.pluxity.facility.facility.Facility;
import com.pluxity.facility.station.Station;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SasangStationMapperTest {

    private final SasangStationMapper sasangStationMapper = SasangStationMapper.INSTANCE;

    @Test
    void toSasangStationResponse_shouldMapCorrectly() {
        // Arrange
        // SasangStation constructor creates Station, which creates Facility.
        // We retrieve these to set their IDs and other specific test values.
        SasangStation sasangStation = new SasangStation(
                "Test Facility Name",   // Name for Facility (via Station constructor)
                "Test Facility Desc", // Description for Facility (via Station constructor)
                "Test Route",           // Route for Station
                "EXT007"                // ExternalCode for SasangStation
        );
        sasangStation.setId(100L);

        assertNotNull(sasangStation.getStation(), "Station should be created by SasangStation constructor");
        Station internalStation = sasangStation.getStation();
        internalStation.setId(10L); // Set ID for the composed Station

        assertNotNull(internalStation.getFacility(), "Facility should be created by Station constructor");
        Facility internalFacility = internalStation.getFacility();
        internalFacility.setId(1L); // Set ID for the composed Facility
        internalFacility.setCode("F001"); // Set Code for the facility part

        // Act
        SasangStationResponse response = sasangStationMapper.toSasangStationResponse(sasangStation);

        // Assert
        assertNotNull(response);
        assertEquals(sasangStation.getId(), response.id());
        assertEquals(sasangStation.getExternalCode(), response.externalCode());

        assertNotNull(response.station(), "Nested StationResponse should not be null");
        assertEquals(internalStation.getId(), response.station().id());
        assertEquals(internalStation.getRoute(), response.station().route());

        assertNotNull(response.station().facility(), "Nested FacilityResponse should not be null");
        assertEquals(internalFacility.getId(), response.station().facility().id());
        assertEquals(internalFacility.getName(), response.station().facility().name());
        assertEquals(internalFacility.getCode(), response.station().facility().code());
    }

    @Test
    void toSasangStationResponse_whenSasangStationIsNull_shouldReturnNull() {
        assertNull(sasangStationMapper.toSasangStationResponse(null));
    }
}
