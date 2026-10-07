package ru.nikzarch.firstService.repository;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;
import ru.nikzarch.firstService.model.CityEntity;

@Repository
public interface CityRepository extends CrudRepository<CityEntity, Integer> {
}
