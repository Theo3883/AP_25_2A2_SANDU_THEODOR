package com.backend.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CountryDTO {
    private Integer id;
    private String name;
    private String code;
    private Integer continentId;
    private String continentName;
} 