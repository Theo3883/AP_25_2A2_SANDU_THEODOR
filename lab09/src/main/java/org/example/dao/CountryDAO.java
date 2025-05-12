package org.example.dao;

import org.example.model.Country;

import java.util.List;

public interface CountryDAO extends AbstractDAO<Country, Integer> {
    List<Country> findByName(String namePattern);
    List<Country> findByContinent(Integer continentId);
    Country findByCode(String code);
}
