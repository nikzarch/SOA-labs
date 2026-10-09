package ru.nikzarch.firstService.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import ru.nikzarch.firstService.dto.CityCreateRequestDto;
import ru.nikzarch.firstService.dto.CityPatchRequestDto;
import ru.nikzarch.firstService.dto.CityResponseDto;
import ru.nikzarch.firstService.error.ApiException;
import ru.nikzarch.firstService.model.CityEntity;
import ru.nikzarch.firstService.model.CoordinatesEntity;
import ru.nikzarch.firstService.model.HumanEntity;
import ru.nikzarch.firstService.repository.CityRepository;


@ApplicationScoped
public class CityService {
    @Inject
    CityRepository repository;

    @Transactional
    public CityResponseDto create(CityCreateRequestDto request) {
        CityEntity entity;
        try {
            entity = CityMapper.toEntity(request);
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest(ex.getMessage());
        }
        return CityMapper.toResponse(repository.insert(entity));
    }

    @Transactional
    public CityResponseDto get(int id) {
        return repository.findById(id)
                .map(CityMapper::toResponse)
                .orElseThrow(() -> ApiException.notFound("Объект с указанным id не найден"));
    }

    @Transactional
    public CityResponseDto update(int id, CityPatchRequestDto request) {
        CityEntity entity = repository.findById(id)
                .orElseThrow(() -> ApiException.notFound(
                        "Объект с указанным id не найден"
                ));

        CityEntity parsed;
        try {
            parsed = CityMapper.toEntity(request);
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest(ex.getMessage());
        }

        var name = parsed.getName();
        if (name != null) {
            entity.setName(name);
        }

        var coordinates = parsed.getCoordinates();
        if (coordinates != null) {
            if (entity.getCoordinates() == null) {
                entity.setCoordinates(new CoordinatesEntity());
            }

            if (coordinates.getX() != null) {
                entity.getCoordinates().setX(coordinates.getX());
            }

            if (coordinates.getY() != null) {
                entity.getCoordinates().setY(coordinates.getY());
            }
        }

        var area = parsed.getArea();
        if (area != null) {
            entity.setArea(area);
        }

        var population = parsed.getPopulation();
        if (population != null) {
            entity.setPopulation(population);
        }

        var metersAboveSeaLevel = parsed.getMetersAboveSeaLevel();
        if (metersAboveSeaLevel != null) {
            entity.setMetersAboveSeaLevel(metersAboveSeaLevel);
        }

        var climate = parsed.getClimate();
        if (climate != null) {
            entity.setClimate(climate);
        }

        var government = parsed.getGovernment();
        if (government != null) {
            entity.setGovernment(government);
        }

        var standardOfLiving = parsed.getStandardOfLiving();
        if (standardOfLiving != null) {
            entity.setStandardOfLiving(standardOfLiving);
        }

        var governor = parsed.getGovernor();
        if (governor != null && governor.getAge() != null) {
            if (entity.getGovernor() == null) {
                entity.setGovernor(new HumanEntity());
            }

            entity.getGovernor().setAge(governor.getAge());
        }

        return CityMapper.toResponse(repository.update(entity));
    }

    @Transactional
    public void delete(int id) {
        repository.deleteById(id);
    }
}
