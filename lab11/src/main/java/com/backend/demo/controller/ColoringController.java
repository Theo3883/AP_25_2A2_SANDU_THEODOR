package com.backend.demo.controller;

import com.backend.demo.model.Country;
import com.backend.demo.service.CountryColoringService;
import com.backend.demo.service.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/colors")
public class ColoringController {

    private final CountryColoringService coloringService;
    private final CountryService countryService;

    @Autowired
    public ColoringController(CountryColoringService coloringService, CountryService countryService) {
        this.coloringService = coloringService;
        this.countryService = countryService;
    }

    @PostMapping("/assign")
    public ResponseEntity<Map<String, Object>> assignColorsToCountries() {
        int colorsUsed = coloringService.assignColorsToCountries();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("colorsUsed", colorsUsed);
        response.put("message", "Successfully assigned colors to countries");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/countries")
    public ResponseEntity<List<Map<String, Object>>> getColoredCountries() {
        List<Country> countries = coloringService.getAllColoredCountries();
        
        List<Map<String, Object>> result = countries.stream()
            .map(country -> {
                Map<String, Object> countryData = new HashMap<>();
                countryData.put("id", country.getId());
                countryData.put("name", country.getName());
                countryData.put("code", country.getCode());
                countryData.put("continent", country.getContinent() != null ? country.getContinent().getName() : null);
                countryData.put("color", country.getColor() != null && !country.getColor().isEmpty() ? 
                        country.getColor() : "#CCCCCC");
                
                if (country.getNeighbors() != null && !country.getNeighbors().isEmpty()) {
                    List<Long> neighborIds = country.getNeighbors().stream()
                        .map(neighbor -> Long.valueOf(neighbor.getId()))
                        .collect(Collectors.toList());
                    countryData.put("neighborIds", neighborIds);
                } else {
                    countryData.put("neighborIds", List.of());
                }
                
                return countryData;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetColors() {
        countryService.resetAllColors();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Successfully reset all country colors");
        
        return ResponseEntity.ok(response);
    }
}
