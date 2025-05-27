package com.backend.demo.utils;

import com.backend.demo.dto.CityDTO;
import com.backend.demo.dto.ContinentDTO;
import com.backend.demo.dto.CountryDTO;
import com.backend.demo.model.City;
import com.backend.demo.model.Continent;
import com.backend.demo.model.Country;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class Mapper {
    private static final int MAX_NEIGHBORS_PER_COUNTRY = 5;

    public ContinentDTO toContinentDTO(Continent continent) {
        return new ContinentDTO(
                continent.getId(),
                continent.getName()
        );
    }

    public List<ContinentDTO> toContinentDTOs(List<Continent> continents) {
        return continents.stream()
                .map(this::toContinentDTO)
                .collect(Collectors.toList());
    }

    public CountryDTO toCountryDTO(Country country) {
        List<Integer> neighborIds = null;
        if (country.getNeighbors() != null && !country.getNeighbors().isEmpty()) {
            neighborIds = limitNeighbors(country.getNeighbors())
                    .stream()
                    .map(Country::getId)
                    .collect(Collectors.toList());
        }

        return new CountryDTO(
                country.getId(),
                country.getName(),
                country.getCode(),
                country.getContinent() != null ? country.getContinent().getId() : null,
                country.getContinent() != null ? country.getContinent().getName() : null,
                country.getColor(),
                neighborIds != null ? neighborIds : List.of()
        );
    }

    private List<Country> limitNeighbors(List<Country> neighbors) {
        if (neighbors.size() <= MAX_NEIGHBORS_PER_COUNTRY) {
            return neighbors;
        }
        return neighbors.stream()
                .limit(MAX_NEIGHBORS_PER_COUNTRY)
                .collect(Collectors.toList());
    }

    public List<CountryDTO> toCountryDTOs(List<Country> countries) {
        return countries.stream()
                .map(this::toCountryDTO)
                .collect(Collectors.toList());
    }

    public CityDTO toCityDTO(City city) {
        return new CityDTO(
                city.getId(),
                city.getName(),
                city.getCountry().getId(),
                city.getCountry().getName(),
                city.getIsCapital(),
                city.getLatitude(),
                city.getLongitude(),
                city.getPopulation()
        );
    }

    public List<CityDTO> toCityDTOs(List<City> cities) {
        return cities.stream()
                .map(this::toCityDTO)
                .collect(Collectors.toList());
    }
}