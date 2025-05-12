package org.example.mapper;

import org.example.dto.CountryDTO;
import org.example.model.Continent;
import org.example.model.Country;

public class CountryMapper implements EntityMapper<CountryDTO, Country> {

    @Override
    public CountryDTO toDto(Country country) {
        if (country == null) {
            return null;
        }

        CountryDTO dto = new CountryDTO();
        dto.setId(country.getId());
        dto.setName(country.getName());
        dto.setCode(country.getCode());

        if (country.getContinent() != null) {
            dto.setContinentId(country.getContinent().getId());
            dto.setContinentName(country.getContinent().getName());
        }

        return dto;
    }

    @Override
    public Country toEntity(CountryDTO dto) {
        if (dto == null) {
            return null;
        }

        Country country = new Country();
        country.setId(dto.getId());
        country.setName(dto.getName());
        country.setCode(dto.getCode());
        return country;
    }

    public Country toEntity(CountryDTO dto, Continent continent) {
        Country country = toEntity(dto);
        if (country != null) {
            country.setContinent(continent);
        }
        return country;
    }
}