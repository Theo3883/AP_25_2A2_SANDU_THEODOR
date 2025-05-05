package org.example.dao;

import org.example.model.City;
import java.util.List;

public interface CityDAO extends AbstractDAO<City, Integer> {
    List<City> findByName(String namePattern);
    List<City> findByCountry(Integer countryId);
    List<City> findCapitals();
    List<City> findByCoordinatesRange(Double minLat, Double maxLat, Double minLong, Double maxLong);
    List<City> findByFirstLetter(char letter);
    List<City> findByPopulationRange(Integer min, Integer max);
}