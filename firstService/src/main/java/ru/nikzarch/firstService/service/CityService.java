package ru.nikzarch.firstService.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import ru.nikzarch.firstService.dto.CityCreateRequestDto;
import ru.nikzarch.firstService.dto.CityPatchRequestDto;
import ru.nikzarch.firstService.dto.CityResponseDto;
import ru.nikzarch.firstService.error.ApiException;
import ru.nikzarch.firstService.error.ErrorCode;
import ru.nikzarch.firstService.model.CityEntity;
import ru.nikzarch.firstService.repository.CityRepository;

@ApplicationScoped
public class CityService {
    @Inject
    CityRepository repository;

    @Inject
    CityMapper mapper;

    @Transactional
    public CityResponseDto create(CityCreateRequestDto request) {
        CityEntity entity = mapper.toEntity(request);
        return mapper.toResponse(repository.insert(entity));
    }

    @Transactional
    public CityResponseDto get(int id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.OBJECT_NOT_FOUND));
    }

    @Transactional
    public CityResponseDto update(int id, CityPatchRequestDto request) {
        CityEntity entity = repository.findById(id)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.OBJECT_NOT_FOUND));
        mapper.updateEntity(request, entity);
        return mapper.toResponse(repository.update(entity));
    }

    @Transactional
    public void delete(int id) {
        repository.deleteById(id);
    }
}
