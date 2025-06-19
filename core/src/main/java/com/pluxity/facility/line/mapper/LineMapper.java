package com.pluxity.facility.line.mapper;

import com.pluxity.facility.line.Line;
import com.pluxity.facility.line.dto.LineCreateRequest;
import com.pluxity.facility.line.dto.LineResponse;
import com.pluxity.facility.line.dto.LineUpdateRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface LineMapper {

    LineMapper INSTANCE = Mappers.getMapper(LineMapper.class);

    // Line Entity to LineResponse DTO
    LineResponse toLineResponse(Line line);

    // LineCreateRequest DTO to Line Entity
    // The Line entity has a @Builder on its constructor (String name, String color)
    // MapStruct should be able to use the builder or constructor if fields match.
    Line fromLineCreateRequest(LineCreateRequest request);

    // Update Line Entity from LineUpdateRequest DTO
    // This will only update non-null fields from the request DTO.
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateLineFromRequest(LineUpdateRequest request, @MappingTarget Line line);
}
