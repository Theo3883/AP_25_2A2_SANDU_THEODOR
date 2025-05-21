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
        return new CountryDTO(
                country.getId(),
                country.getName(),
                country.getCode(),
                country.getContinent().getId(),
                country.getContinent().getName()
        );
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