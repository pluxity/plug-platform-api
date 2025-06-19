package com.pluxity.facility.facility.mapper;

import com.pluxity.facility.facility.Facility;
import com.pluxity.facility.facility.dto.FacilityResponse;
// import com.pluxity.facility.facility.dto.FacilityCreateRequest; // Not implementing these yet
// import com.pluxity.facility.facility.dto.FacilityUpdateRequest;
import org.mapstruct.Mapper;
// import org.mapstruct.MappingTarget; // Not needed for current methods
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface FacilityMapper {

    FacilityMapper INSTANCE = Mappers.getMapper(FacilityMapper.class);

    // Fields in Facility: id, code, name, description, drawingFileId, thumbnailFileId
    // Fields in FacilityResponse (simplified): id, code, name, description, drawingFileId, thumbnailFileId
    // Direct mapping should work.
    FacilityResponse toFacilityResponse(Facility facility);

    // TODO: Consider adding mappings if Create/Update requests are needed for Facility directly
    // Facility toFacility(FacilityCreateRequest request);
    // void updateFacilityFromRequest(FacilityUpdateRequest request, @MappingTarget Facility facility);
}
