package com.backend.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CityDTO {
    private Integer id;
    private String name;
    private Integer countryId;
    private String countryName;
    private Boolean isCapital;
    private Double latitude;
    private Double longitude;
    private Integer population;
} 