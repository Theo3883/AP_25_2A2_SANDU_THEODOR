package org.example.dao.impl.jpa;

import jakarta.persistence.TypedQuery;
import org.example.dao.CountryDAO;
import org.example.model.Country;

import java.util.List;

public class JPACountryDAO extends JPAAbstractDAO<Country, Integer> implements CountryDAO {

    @Override
    public List<Country> findByName(String namePattern) {
        TypedQuery<Country> query = entityManager.createQuery(
                "SELECT c FROM Country c WHERE c.name LIKE :namePattern", Country.class);
        query.setParameter("namePattern", namePattern);
        return query.getResultList();
    }

    @Override
    public List<Country> findByContinent(Integer continentId) {
        TypedQuery<Country> query = entityManager.createQuery(
                "SELECT c FROM Country c WHERE c.continent.id = :continentId", Country.class);
        query.setParameter("continentId", continentId);
        return query.getResultList();
    }

    @Override
    public Country findByCode(String code) {
        TypedQuery<Country> query = entityManager.createQuery(
                "SELECT c FROM Country c WHERE c.code = :code", Country.class);
        query.setParameter("code", code);
        return query.getSingleResult();
    }
}
