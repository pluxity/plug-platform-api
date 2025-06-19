package com.pluxity.facility.station.mapper;

import com.pluxity.facility.facility.Facility;
// FacilityResponse is not directly used here, but FacilityMapper which produces it is used by StationMapper
import com.pluxity.facility.station.Station;
import com.pluxity.facility.station.dto.StationResponse;
import com.pluxity.facility.line.Line;
import com.pluxity.facility.station.StationLine; // StationLine is used by station.addLine
import com.pluxity.feature.entity.Feature;
import org.junit.jupiter.api.Test;
import java.util.List; // For Collections.emptyList() if used
import java.util.Collections; // For Collections.emptyList()
import static org.junit.jupiter.api.Assertions.*;

class StationMapperTest {

    private final StationMapper stationMapper = StationMapper.INSTANCE; // Assumes FacilityMapper is also available via INSTANCE if needed by StationMapper

    @Test
    void toStationResponse_shouldMapCorrectly() {
        // Arrange
        // Station constructor will create an internal Facility. We get it and modify it.
        Station station = new Station("Station Name FacPart", "Station Desc FacPart", "Route 1");
        station.setId(10L);
        station.setSubway("Subway Line A"); // Assuming there's a setSubway or it's part of constructor

        // Get the internally created Facility and set its properties
        Facility internalFacility = station.getFacility(); // Assumes getFacility() is public
        assertNotNull(internalFacility, "Facility should be created by Station constructor");
        internalFacility.setId(1L); // Set ID for the facility part
        internalFacility.setCode("F001"); // Set Code for the facility part

        Feature feature1 = new Feature();
        feature1.setId("feat1");
        internalFacility.addFeature(feature1); // Add feature to the internal facility

        Line line1 = new Line("Line 1");
        line1.setId(100L);
        station.addLine(line1);

        // Act
        StationResponse response = stationMapper.toStationResponse(station);

        // Assert
        assertNotNull(response);
        assertEquals(station.getId(), response.id());
        assertEquals(station.getRoute(), response.route());
        assertEquals(station.getSubway(), response.subway());

        assertNotNull(response.facility());
        assertEquals(internalFacility.getId(), response.facility().id());
        assertEquals(internalFacility.getName(), response.facility().name()); // Name comes from Station constructor's args
        assertEquals(internalFacility.getCode(), response.facility().code());


        assertNotNull(response.lineIds());
        assertEquals(1, response.lineIds().size());
        assertEquals(line1.getId(), response.lineIds().get(0));

        assertNotNull(response.featureIds());
        assertEquals(1, response.featureIds().size());
        assertEquals(feature1.getId(), response.featureIds().get(0));

        // Floors are ignored by mapper. Check for null or empty list based on record initialization.
        // The StationResponse record does not initialize floors, so it will be null if not set.
        // MapStruct sets it to null if ignored and not otherwise provided.
        assertNull(response.floors());
    }

    @Test
    void toStationResponse_whenStationIsNull_shouldReturnNull() {
        assertNull(stationMapper.toStationResponse(null));
    }

    @Test
    void toStationResponse_withNullCollections_shouldMapToEmptyLists() {
        // Arrange
        Station station = new Station("Station Name", "Station Desc", "Route 1");
        station.setId(10L);
        // station.stationLines is final and initialized to new ArrayList<>()
        // station.getFacility().getFeatures() is final and initialized to new ArrayList<>()

        // Act
        StationResponse response = stationMapper.toStationResponse(station);

        // Assert
        assertNotNull(response);
        assertNotNull(response.lineIds(), "Line IDs should be an empty list, not null");
        assertTrue(response.lineIds().isEmpty(), "Line IDs should be empty");

        assertNotNull(response.featureIds(), "Feature IDs should be an empty list, not null");
        assertTrue(response.featureIds().isEmpty(), "Feature IDs should be empty");
    }
}
