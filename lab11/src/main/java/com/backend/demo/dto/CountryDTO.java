package com.backend.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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
    private String color;
    private List<Integer> neighborIds;
}