package org.example.dto;

import lombok.Data;

@Data
public class CountryDTO {
    private Integer id;
    private String name;
    private String code;
    private Integer continentId;
    private String continentName;
}