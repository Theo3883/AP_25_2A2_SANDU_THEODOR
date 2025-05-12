package org.example.dao.impl.jpa;

import jakarta.persistence.TypedQuery;
import org.example.dao.CityDAO;
import org.example.model.City;

import java.util.List;

public class JPACityDAO extends JPAAbstractDAO<City, Integer> implements CityDAO {

    @Override
    public List<City> findByName(String namePattern) {
        TypedQuery<City> query = entityManager.createQuery(
                "SELECT c FROM City c WHERE c.name LIKE :namePattern", City.class);
        query.setParameter("namePattern", namePattern);
        return query.getResultList();
    }

    @Override
    public List<City> findByCountry(Integer countryId) {
        TypedQuery<City> query = entityManager.createQuery(
                "SELECT c FROM City c WHERE c.country.id = :countryId", City.class);
        query.setParameter("countryId", countryId);
        return query.getResultList();
    }

    @Override
    public List<City> findCapitals() {
        TypedQuery<City> query = entityManager.createQuery(
                "SELECT c FROM City c WHERE c.isCapital = true", City.class);
        return query.getResultList();
    }

    @Override
    public List<City> findByCoordinatesRange(Double minLat, Double maxLat, Double minLong, Double maxLong) {
        TypedQuery<City> query = entityManager.createQuery(
                "SELECT c FROM City c WHERE c.latitude BETWEEN :minLat AND :maxLat " +
                        "AND c.longitude BETWEEN :minLong AND :maxLong", City.class);
        query.setParameter("minLat", minLat);
        query.setParameter("maxLat", maxLat);
        query.setParameter("minLong", minLong);
        query.setParameter("maxLong", maxLong);
        return query.getResultList();
    }

    @Override
    public List<City> findByFirstLetter(char letter) {
        TypedQuery<City> query = entityManager.createQuery(
                "SELECT c FROM City c WHERE c.name LIKE :letter", City.class);
        query.setParameter("letter", letter + "%");
        return query.getResultList();
    }

    @Override
    public List<City> findByPopulationRange(Integer min, Integer max) {
        TypedQuery<City> query = entityManager.createQuery(
                "SELECT c FROM City c WHERE c.population BETWEEN :min AND :max", City.class);
        query.setParameter("min", min);
        query.setParameter("max", max);
        return query.getResultList();
    }
}
