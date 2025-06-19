package com.pluxity.domains.sasang.station.mapper;

import com.pluxity.domains.sasang.station.SasangStation;
import com.pluxity.domains.sasang.station.dto.SasangStationResponse;
import com.pluxity.facility.station.mapper.StationMapper; // Uses StationMapper from core
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {StationMapper.class})
public interface SasangStationMapper {

    SasangStationMapper INSTANCE = Mappers.getMapper(SasangStationMapper.class);

    // SasangStation.id -> SasangStationResponse.id (auto)
    // SasangStation.externalCode -> SasangStationResponse.externalCode (auto)
    // SasangStation.station (entity) -> SasangStationResponse.station (DTO) via StationMapper
    @Mapping(source = "station", target = "station") // Map SasangStation.station (entity) to SasangStationResponse.station (DTO)
    SasangStationResponse toSasangStationResponse(SasangStation sasangStation);
}
