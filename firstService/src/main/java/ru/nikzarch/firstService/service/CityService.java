package ru.nikzarch.firstService.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import ru.nikzarch.firstService.api.CityRequest;
import ru.nikzarch.firstService.api.CityResponse;
import ru.nikzarch.firstService.error.ApiException;
import ru.nikzarch.firstService.model.CityEntity;
import ru.nikzarch.firstService.repository.CityRepository;


@ApplicationScoped
public class CityService {
    @Inject
    CityRepository repository;

    @Transactional
    public CityResponse create(CityRequest request) {
        CityEntity entity;
        try {
            entity = CityMapper.toEntity(request);
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest(ex.getMessage());
        }
        return CityMapper.toResponse(repository.insert(entity));
    }

    @Transactional
    public CityResponse get(int id) {
        return repository.findById(id)
                .map(CityMapper::toResponse)
                .orElseThrow(() -> ApiException.notFound("Объект с указанным id не найден"));
    }

    @Transactional
    public CityResponse update(int id, CityRequest request) {
        CityEntity entity = repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Объект с указанным id не найден"));
        try {
            CityMapper.copyToEntity(request, entity);
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest(ex.getMessage());
        }
        entity.setId(id);
        return CityMapper.toResponse(repository.update(entity));
    }

    @Transactional
    public void delete(int id) {
        repository.deleteById(id);
    }
}
