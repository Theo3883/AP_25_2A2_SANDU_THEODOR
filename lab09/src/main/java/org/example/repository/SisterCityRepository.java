package org.example.repository;

import jakarta.persistence.EntityManager;
import org.example.model.SisterCity;

import java.util.List;

public class SisterCityRepository extends AbstractRepository<SisterCity, Integer> {

    public SisterCityRepository(EntityManager entityManager) {
        super(entityManager);
    }

    public List<SisterCity> findByCityId(Integer cityId) {
        return findByJpql(
                "SELECT sc FROM SisterCity sc WHERE sc.city1.id = :cityId OR sc.city2.id = :cityId",
                "cityId", cityId
        );
    }

    public List<SisterCity> findByCountryId(Integer countryId) {
        return findByJpql(
                "SELECT sc FROM SisterCity sc WHERE sc.city1.country.id = :countryId OR sc.city2.country.id = :countryId",
                "countryId", countryId
        );
    }

    public List<SisterCity> findByContinentId(Integer continentId) {
        return findByJpql(
                "SELECT sc FROM SisterCity sc WHERE sc.city1.country.continent.id = :continentId " +
                        "OR sc.city2.country.continent.id = :continentId",
                "continentId", continentId
        );
    }
}