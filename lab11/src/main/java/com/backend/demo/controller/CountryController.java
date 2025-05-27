package com.backend.demo.controller;

import com.backend.demo.dto.CountryDTO;
import com.backend.demo.model.Country;
import com.backend.demo.service.CountryColoringService;
import com.backend.demo.service.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/countries")
public class CountryController {

    private final CountryColoringService countryColoringService;
    private final CountryService countryService;

    @Autowired
    public CountryController(CountryColoringService countryColoringService, CountryService countryService) {
        this.countryColoringService = countryColoringService;
        this.countryService = countryService;
    }

    @PostMapping("/assign-colors")
    @PreAuthorize("hasAnyRole('ADMIN', 'API_CLIENT')")
    public ResponseEntity<?> assignColors() {
        countryColoringService.assignColorsToCountries();
        return ResponseEntity.ok("Colors assigned successfully to countries.");
    }
    
    @GetMapping("/graph-data")
    public ResponseEntity<List<CountryDTO>> getCountriesGraphData() {
        List<Country> countries = countryColoringService.getAllColoredCountries();
        
        List<CountryDTO> countryDTOs = countries.stream()
            .map(country -> {
                CountryDTO dto = new CountryDTO();
                dto.setId(country.getId());
                dto.setName(country.getName());
                dto.setCode(country.getCode());
                dto.setContinentId(country.getContinent() != null ? country.getContinent().getId() : null);
                dto.setContinentName(country.getContinent() != null ? country.getContinent().getName() : null);
                
                dto.setColor(country.getColor() != null ? country.getColor() : "#CCCCCC");
                
                if (country.getNeighbors() != null) {
                    List<Integer> neighborIds = country.getNeighbors().stream()
                        .map(Country::getId)
                        .collect(Collectors.toList());
                    dto.setNeighborIds(neighborIds);
                } else {
                    dto.setNeighborIds(new ArrayList<>());
                }
                
                return dto;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(countryDTOs);
    }

    @PostMapping("/reset-colors")
    @PreAuthorize("hasAnyRole('ADMIN', 'API_CLIENT')")
    public ResponseEntity<?> resetColors() {
        countryService.resetAllColors();
        return ResponseEntity.ok("All country colors have been reset");
    }
}
