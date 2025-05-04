package org.example.mapper;

import org.example.dto.ContinentDTO;
import org.example.model.Continent;

public class ContinentMapper implements EntityMapper<ContinentDTO, Continent> {

    @Override
    public ContinentDTO toDto(Continent continent) {
        if (continent == null) {
            return null;
        }

        ContinentDTO dto = new ContinentDTO();
        dto.setId(continent.getId());
        dto.setName(continent.getName());
        return dto;
    }

    @Override
    public Continent toEntity(ContinentDTO dto) {
        if (dto == null) {
            return null;
        }

        Continent continent = new Continent();
        continent.setId(dto.getId());
        continent.setName(dto.getName());
        return continent;
    }
}