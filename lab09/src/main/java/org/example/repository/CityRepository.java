package org.example.repository;

import jakarta.persistence.EntityManager;
import org.example.model.City;

import java.util.List;

public class CityRepository extends AbstractRepository<City, Integer> {

    public CityRepository(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public List<City> findAll() {
        return findAllOrderedBy("name");
    }

    public List<City> findByCountry(Integer countryId) {
        return findByJpql(
                "SELECT c FROM City c WHERE c.country.id = :countryId ORDER BY c.name",
                "countryId", countryId
        );
    }

    public List<City> findCapitals() {
        return findByJpql(
                "SELECT c FROM City c WHERE c.isCapital = true ORDER BY c.name"
        );
    }

    public List<City> findByNameContaining(String namePart) {
        return findByJpql(
                "SELECT c FROM City c WHERE LOWER(c.name) LIKE LOWER(:namePart) ORDER BY c.name",
                "namePart", "%" + namePart + "%"
        );
    }

    public List<City> findByCoordinatesRange(Double minLat, Double maxLat, Double minLong, Double maxLong) {
        return findByJpql(
                "SELECT c FROM City c WHERE c.latitude BETWEEN :minLat AND :maxLat " +
                        "AND c.longitude BETWEEN :minLong AND :maxLong ORDER BY c.name",
                "minLat", minLat, "maxLat", maxLat, "minLong", minLong, "maxLong", maxLong
        );
    }
}