package ru.nikzarch.firstService.service;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.nikzarch.firstService.dto.CityCreateRequestDto;
import ru.nikzarch.firstService.dto.CityPatchRequestDto;
import ru.nikzarch.firstService.dto.CityResponseDto;
import ru.nikzarch.firstService.model.CityEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface CityMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "coordinateX", source = "coordinates.x")
    @Mapping(target = "coordinateY", source = "coordinates.y")
    @Mapping(target = "governorAge", source = "governor.age")
    CityEntity toEntity(CityCreateRequestDto request);

    @Mapping(target = "coordinates.x", source = "coordinateX")
    @Mapping(target = "coordinates.y", source = "coordinateY")
    @Mapping(target = "governor.age", source = "governorAge")
    CityResponseDto toResponse(CityEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "coordinateX", source = "coordinates.x")
    @Mapping(target = "coordinateY", source = "coordinates.y")
    @Mapping(target = "governorAge", source = "governor.age")
    void updateEntity(CityPatchRequestDto request, @MappingTarget CityEntity entity);
}
