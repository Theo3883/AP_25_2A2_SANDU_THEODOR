
package org.example.mapper;

import org.example.dto.CityDTO;
import org.example.model.City;
import org.example.model.Country;

public class CityMapper implements EntityMapper<CityDTO, City> {

    @Override
    public CityDTO toDto(City city) {
        if (city == null) {
            return null;
        }

        CityDTO dto = new CityDTO();
        dto.setId(city.getId());
        dto.setName(city.getName());
        dto.setIsCapital(city.getIsCapital());
        dto.setLatitude(city.getLatitude());
        dto.setLongitude(city.getLongitude());

        if (city.getCountry() != null) {
            dto.setCountryId(city.getCountry().getId());
            dto.setCountryName(city.getCountry().getName());
        }

        return dto;
    }

    @Override
    public City toEntity(CityDTO dto) {
        if (dto == null) {
            return null;
        }

        City city = new City();
        city.setId(dto.getId());
        city.setName(dto.getName());
        city.setIsCapital(dto.getIsCapital());
        city.setLatitude(dto.getLatitude());
        city.setLongitude(dto.getLongitude());

        return city;
    }

    public City toEntity(CityDTO dto, Country country) {
        City city = toEntity(dto);
        if (city != null) {
            city.setCountry(country);
        }
        return city;
    }
}