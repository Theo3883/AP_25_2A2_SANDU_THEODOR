package com.backend.demo.controller;

import com.backend.demo.service.CountryColoringService;
import com.backend.demo.service.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

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
    
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetColors() {
        countryService.resetAllColors();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Successfully reset all country colors");
        
        return ResponseEntity.ok(response);
    }
}
