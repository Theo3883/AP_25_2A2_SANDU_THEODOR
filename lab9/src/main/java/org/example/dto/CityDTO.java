package org.example.dto;

import lombok.Data;

@Data
public class CityDTO {
    private Integer id;
    private String name;
    private Boolean isCapital;
    private Double latitude;
    private Double longitude;
    private Integer countryId;
    private String countryName;
}