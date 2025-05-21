package com.backend.demo.controller;

import com.backend.demo.dto.ContinentDTO;
import com.backend.demo.dto.ContinentNameUpdateDTO;
import com.backend.demo.service.ContinentService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/continents")
@Tag(name = "Continent Management", description = "Operations related to continents")
public class ContinentController {
    
    private final ContinentService continentService;
    
    @Autowired
    public ContinentController(ContinentService continentService) {
        this.continentService = continentService;
    }

    @GetMapping
    public ResponseEntity<List<ContinentDTO>> getAllContinents() {
        List<ContinentDTO> continents = continentService.getAllContinents();
        return ResponseEntity.ok(continents);
    }

    @PostMapping
    public ResponseEntity<ContinentDTO> createContinent(
            @Parameter(description = "Continent to be created", required = true, schema = @Schema(implementation = ContinentDTO.class))
            @RequestBody ContinentDTO continentDTO) {
        ContinentDTO createdContinent = continentService.createContinent(continentDTO);
        
        if (createdContinent != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(createdContinent);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContinentDTO> updateContinentName(
            @Parameter(description = "Continent ID", required = true) @PathVariable Integer id, 
            @Parameter(description = "Updated continent name", required = true, schema = @Schema(implementation = ContinentNameUpdateDTO.class))
            @RequestBody ContinentNameUpdateDTO updateDTO) {
        // Validate input
        if (updateDTO.getName() == null || updateDTO.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        ContinentDTO updatedContinent = continentService.updateContinentName(id, updateDTO.getName());
        
        if (updatedContinent != null) {
            return ResponseEntity.ok(updatedContinent);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContinent(
            @Parameter(description = "Continent ID", required = true) @PathVariable Integer id) {
        if (continentService.deleteContinent(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
} 