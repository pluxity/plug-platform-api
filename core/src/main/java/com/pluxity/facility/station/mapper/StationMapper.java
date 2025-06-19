package com.pluxity.facility.station.mapper;

import com.pluxity.facility.facility.Facility; // Required for featuresToFeatureIds
import com.pluxity.facility.facility.mapper.FacilityMapper;
import com.pluxity.facility.station.Station;
import com.pluxity.facility.station.dto.StationResponse;
import com.pluxity.facility.station.StationLine;
import com.pluxity.facility.line.Line;
import com.pluxity.feature.entity.Feature; // Required for featuresToFeatureIds
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import java.util.Collections; // For empty list
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {FacilityMapper.class})
public interface StationMapper {

    StationMapper INSTANCE = Mappers.getMapper(StationMapper.class);

    // Station.id -> StationResponse.id (auto)
    // Station.facility -> StationResponse.facility (using FacilityMapper) (auto)
    // Station.route -> StationResponse.route (auto)
    // Station.subway -> StationResponse.subway (auto)
    @Mapping(source = "stationLines", target = "lineIds", qualifiedByName = "stationLinesToLineIds")
    @Mapping(source = "facility.features", target = "featureIds", qualifiedByName = "facilityFeaturesToFeatureIds")
    // floors cannot be mapped directly from Station entity, must be handled in service
    @Mapping(target = "floors", ignore = true)
    StationResponse toStationResponse(Station station);

    @Named("stationLinesToLineIds")
    default List<Long> stationLinesToLineIds(List<StationLine> stationLines) {
        if (stationLines == null) {
            return Collections.emptyList();
        }
        return stationLines.stream()
            .map(StationLine::getLine)
            .map(Line::getId)
            .collect(Collectors.toList());
    }

    @Named("facilityFeaturesToFeatureIds")
    default List<String> facilityFeaturesToFeatureIds(List<Feature> features) {
        if (features == null) {
            return Collections.emptyList();
        }
        return features.stream()
            .map(Feature::getId)
            .collect(Collectors.toList());
    }
}
