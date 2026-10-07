package ru.nikzarch.firstService.service;

import ru.nikzarch.firstService.api.CityRequest;
import ru.nikzarch.firstService.api.CityResponse;
import ru.nikzarch.firstService.model.CityEntity;
import ru.nikzarch.firstService.model.Climate;
import ru.nikzarch.firstService.model.CoordinatesEntity;
import ru.nikzarch.firstService.model.Government;
import ru.nikzarch.firstService.model.HumanEntity;
import ru.nikzarch.firstService.model.StandardOfLiving;

public class CityMapper {
    private CityMapper() {}

    public static CityEntity toEntity(CityRequest request) {
        CityEntity entity = new CityEntity();
        copyToEntity(request, entity);
        return entity;
    }

    public static void copyToEntity(CityRequest request, CityEntity entity) {
        entity.setName(request.getName());
        entity.setCoordinates(new CoordinatesEntity(
                request.getCoordinates().getX(),
                request.getCoordinates().getY()));
        entity.setArea(request.getArea());
        entity.setPopulation(request.getPopulation());
        entity.setMetersAboveSeaLevel(request.getMetersAboveSeaLevel());
        entity.setClimate(parseEnum(Climate.class, request.getClimate(), "climate"));
        entity.setGovernment(parseEnum(Government.class, request.getGovernment(), "government"));
        entity.setStandardOfLiving(parseNullableEnum(StandardOfLiving.class, request.getStandardOfLiving(), "standardOfLiving"));
        entity.setGovernor(new HumanEntity(request.getGovernor().getAge()));
    }

    public static CityResponse toResponse(CityEntity entity) {
        CityResponse response = new CityResponse();
        response.setId(entity.getId());
        response.setCreationDate(entity.getCreationDate().toString());
        response.setName(entity.getName());
        var coordinates = new ru.nikzarch.firstService.api.Coordinates();
        coordinates.setX(entity.getCoordinates().getX());
        coordinates.setY(entity.getCoordinates().getY());
        response.setCoordinates(coordinates);
        response.setArea(entity.getArea());
        response.setPopulation(entity.getPopulation());
        response.setMetersAboveSeaLevel(entity.getMetersAboveSeaLevel());
        response.setClimate(entity.getClimate().name());
        response.setGovernment(entity.getGovernment().name());
        response.setStandardOfLiving(entity.getStandardOfLiving() == null ? null : entity.getStandardOfLiving().name());
        var governor = new ru.nikzarch.firstService.api.Human();
        governor.setAge(entity.getGovernor().getAge());
        response.setGovernor(governor);
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
