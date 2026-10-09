package ru.nikzarch.firstService.service;

import ru.nikzarch.firstService.dto.*;
import ru.nikzarch.firstService.model.CityEntity;
import ru.nikzarch.firstService.model.CoordinatesEntity;
import ru.nikzarch.firstService.model.HumanEntity;

public class CityMapper {
    private CityMapper() {
    }

    public static CityEntity toEntity(CityPatchRequestDto request) {
        CityEntity entity = new CityEntity();

        entity.setName(request.getName());
        entity.setArea(request.getArea());
        entity.setPopulation(request.getPopulation());
        entity.setMetersAboveSeaLevel(request.getMetersAboveSeaLevel());
        entity.setClimate(request.getClimate());
        entity.setGovernment(request.getGovernment());
        entity.setStandardOfLiving(request.getStandardOfLiving());

        if (request.getCoordinates() != null) {
            CoordinatesEntity coordinates = new CoordinatesEntity();
            coordinates.setX(Math.toIntExact(request.getCoordinates().getX()));
            coordinates.setY(request.getCoordinates().getY());
            entity.setCoordinates(coordinates);
        }

        if (request.getGovernor() != null) {
            HumanEntity governor = new HumanEntity();
            governor.setAge(Math.toIntExact(request.getGovernor().getAge()));
            entity.setGovernor(governor);
        }

        return entity;
    }

    public static CityEntity toEntity(CityCreateRequestDto request) {
        CityEntity entity = new CityEntity();

        entity.setName(request.getName());
        entity.setArea(request.getArea());
        entity.setPopulation(request.getPopulation());
        entity.setMetersAboveSeaLevel(request.getMetersAboveSeaLevel());
        entity.setClimate(request.getClimate());
        entity.setGovernment(request.getGovernment());
        entity.setStandardOfLiving(request.getStandardOfLiving());

        if (request.getCoordinates() != null) {
            CoordinatesEntity coordinates = new CoordinatesEntity();
            coordinates.setX(Math.toIntExact(request.getCoordinates().getX()));
            coordinates.setY(request.getCoordinates().getY());
            entity.setCoordinates(coordinates);
        }

        if (request.getGovernor() != null) {
            HumanEntity governor = new HumanEntity();
            governor.setAge(Math.toIntExact(request.getGovernor().getAge()));
            entity.setGovernor(governor);
        }

        return entity;
    }

    public static CityResponseDto toResponse(CityEntity entity) {
        CityResponseDto response = new CityResponseDto();

        response.setId(entity.getId());
        response.setCreationDate(entity.getCreationDate().toString());
        response.setName(entity.getName());

        var coordinates = new CoordinatesDto();
        coordinates.setX(Long.valueOf(entity.getCoordinates().getX()));
        coordinates.setY(entity.getCoordinates().getY());
        response.setCoordinates(coordinates);

        response.setArea(entity.getArea());
        response.setPopulation(entity.getPopulation());
        response.setMetersAboveSeaLevel(entity.getMetersAboveSeaLevel());

        response.setClimate(
                entity.getClimate() == null ? null : entity.getClimate()
        );
        response.setGovernment(
                entity.getGovernment() == null ? null : entity.getGovernment()
        );
        response.setStandardOfLiving(
                entity.getStandardOfLiving() == null
                        ? null
                        : entity.getStandardOfLiving()
        );

        if (entity.getGovernor() != null) {
            var governor = new HumanDto();
            governor.setAge(Long.valueOf(entity.getGovernor().getAge()));
            response.setGovernor(governor);
        } else {
            response.setGovernor(null);
        }

        return response;
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " must not be null");
        }
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Недопустимое значение для поля " + field + ": " + value, ex);
        }
    }

    private static <E extends Enum<E>> E parseNullableEnum(Class<E> type, String value, String field) {
        if (value == null) return null;
        return parseEnum(type, value, field);
    }
}
