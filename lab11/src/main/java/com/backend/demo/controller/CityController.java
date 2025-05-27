package com.backend.demo.controller;

import com.backend.demo.dto.CityDTO;
import com.backend.demo.dto.NameUpdateDTO;
import com.backend.demo.service.CityService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@Tag(name = "City Management", description = "Operations related to cities")
public class CityController {
    
    private final CityService cityService;
    
    @Autowired
    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @GetMapping
    public ResponseEntity<List<CityDTO>> getAllCities() {
        List<CityDTO> cities = cityService.getAllCities();
        return ResponseEntity.ok(cities);
    }

    @PostMapping
    public ResponseEntity<CityDTO> createCity(
            @Parameter(description = "City to be created", required = true, schema = @Schema(implementation = CityDTO.class))
            @RequestBody CityDTO cityDTO) {
        CityDTO createdCity = cityService.createCity(cityDTO);
        
        if (createdCity != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCity);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CityDTO> updateCityName(
            @Parameter(description = "City ID", required = true) @PathVariable Integer id, 
            @Parameter(description = "Updated city name", required = true, schema = @Schema(implementation = NameUpdateDTO.class))
            @RequestBody NameUpdateDTO updateDTO) {
        if (updateDTO.getName() == null || updateDTO.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        CityDTO updatedCity = cityService.updateCityName(id, updateDTO.getName());
        
        if (updatedCity != null) {
            return ResponseEntity.ok(updatedCity);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCity(
            @Parameter(description = "City ID", required = true) @PathVariable Integer id) {
        if (cityService.deleteCity(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}