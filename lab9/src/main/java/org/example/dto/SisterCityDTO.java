package org.example.dto;

import lombok.Data;

@Data
public class SisterCityDTO {
    private Integer id;
    private Integer city1Id;
    private String city1Name;
    private Integer city2Id;
    private String city2Name;
}