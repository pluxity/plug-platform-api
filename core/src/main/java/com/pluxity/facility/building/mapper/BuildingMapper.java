package com.pluxity.facility.building.mapper;

import com.pluxity.facility.building.Building;
import com.pluxity.facility.building.dto.BuildingResponse;
import com.pluxity.facility.facility.mapper.FacilityMapper; // Uses FacilityMapper
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {FacilityMapper.class})
public interface BuildingMapper {

    BuildingMapper INSTANCE = Mappers.getMapper(BuildingMapper.class);

    // Building.facility (entity) will be mapped to BuildingResponse.facility (DTO) by FacilityMapper
    // Building.id to BuildingResponse.id (auto)
    @Mapping(target = "floors", ignore = true) // Floors are handled by FloorStrategy in the service
    BuildingResponse toBuildingResponse(Building building);
}
