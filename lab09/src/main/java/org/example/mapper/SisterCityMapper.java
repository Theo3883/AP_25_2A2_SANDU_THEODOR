package org.example.mapper;

import org.example.dto.SisterCityDTO;
import org.example.model.City;
import org.example.model.SisterCity;


public class SisterCityMapper implements EntityMapper<SisterCityDTO, SisterCity> {

    @Override
    public SisterCityDTO toDto(SisterCity sisterCity) {
        if (sisterCity == null) {
            return null;
        }

        SisterCityDTO dto = new SisterCityDTO();
        dto.setId(sisterCity.getId());

        if (sisterCity.getCity1() != null) {
            dto.setCity1Id(sisterCity.getCity1().getId());
            dto.setCity1Name(sisterCity.getCity1().getName());
        }

        if (sisterCity.getCity2() != null) {
            dto.setCity2Id(sisterCity.getCity2().getId());
            dto.setCity2Name(sisterCity.getCity2().getName());
        }

        return dto;
    }

    @Override
    public SisterCity toEntity(SisterCityDTO dto) {
        if (dto == null) {
            return null;
        }

        SisterCity sisterCity = new SisterCity();
        sisterCity.setId(dto.getId());
        return sisterCity;
    }


    public SisterCity toEntity(SisterCityDTO dto, City city1, City city2) {
        SisterCity sisterCity = toEntity(dto);
        if (sisterCity != null) {
            sisterCity.setCity1(city1);
            sisterCity.setCity2(city2);
        }
        return sisterCity;
    }
}