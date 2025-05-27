package com.backend.demo.service;

import com.backend.demo.model.Country;
import com.backend.demo.repository.CountryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.List;

@Service
public class CountryColoringService {

    private static final Logger logger = LoggerFactory.getLogger(CountryColoringService.class);
    private final CountryRepository countryRepository;
    private final Random random = new Random();
    private final CountryService countryService;

    @Autowired
    public CountryColoringService(CountryRepository countryRepository, CountryService countryService) {
        this.countryRepository = countryRepository;
        this.countryService = countryService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void resetAndAssignColorsOnStartup() {
        logger.info("Application started: Resetting and reassigning colors to countries");
        resetColors();
        assignColorsToCountries();
    }
    
    @Transactional
    public void resetColors() {
        logger.info("Resetting all country colors");
        countryService.resetAllColors();
    }

    @Transactional
    public int assignColorsToCountries() {
        List<Country> countries = countryRepository.findAllWithNeighbors();
        Map<Country, List<Country>> adjacencyMap = buildAdjacencyMap(countries);

        Map<Country, String> countryColors = new HashMap<>();
        int colorsUsed = colorGraph(adjacencyMap, countryColors);

        for (Map.Entry<Country, String> entry : countryColors.entrySet()) {
            Country country = entry.getKey();
            String colorHex = entry.getValue();
            country.setColor(colorHex);
            countryRepository.save(country);
            logger.debug("Assigned color {} to country {}", colorHex, country.getName());
        }

        logger.info("Assigned random colors to countries using {} different colors.", colorsUsed);
        return colorsUsed;
    }

    private Map<Country, List<Country>> buildAdjacencyMap(List<Country> countries) {
        Map<Country, List<Country>> adjacencyMap = new HashMap<>();
        int totalEdges = 0;
        
        for (Country country : countries) {
            List<Country> neighbors = country.getNeighbors();
            adjacencyMap.put(country, neighbors != null ? new ArrayList<>(neighbors) : new ArrayList<>());
            totalEdges += neighbors != null ? neighbors.size() : 0;
        }
        
        logger.info("Built adjacency map with {} countries and {} edges (average degree: {})", 
                countries.size(), totalEdges/2, 
                countries.isEmpty() ? 0 : (double)totalEdges/countries.size());
                
        return adjacencyMap;
    }

    private int colorGraph(Map<Country, List<Country>> adjacencyMap, Map<Country, String> countryColors) {
        List<Country> sortedCountries = new ArrayList<>(adjacencyMap.keySet());
        sortedCountries.sort((c1, c2) -> 
            Integer.compare(adjacencyMap.get(c2).size(), adjacencyMap.get(c1).size()));

        Set<String> usedColors = new HashSet<>();
        
        for (Country country : sortedCountries) {
            Set<String> neighborColors = new HashSet<>();

            for (Country neighbor : adjacencyMap.get(country)) {
                if (countryColors.containsKey(neighbor)) {
                    neighborColors.add(countryColors.get(neighbor));
                }
            }

            String randomColor = generateDistinctRandomColor(neighborColors);
            countryColors.put(country, randomColor);
            usedColors.add(randomColor);
        }

        return usedColors.size();
    }

    private String generateDistinctRandomColor(Set<String> avoidColors) {
        final int MIN_BRIGHTNESS = 50;
        final int MAX_BRIGHTNESS = 230;
        final int MIN_COLOR_DISTANCE = 50;

        String newColor;
        int attempts = 0;
        final int MAX_ATTEMPTS = 100;

        do {
            int r = random.nextInt(MAX_BRIGHTNESS - MIN_BRIGHTNESS) + MIN_BRIGHTNESS;
            int g = random.nextInt(MAX_BRIGHTNESS - MIN_BRIGHTNESS) + MIN_BRIGHTNESS;
            int b = random.nextInt(MAX_BRIGHTNESS - MIN_BRIGHTNESS) + MIN_BRIGHTNESS;

            newColor = String.format("#%02X%02X%02X", r, g, b);
            attempts++;

            if (attempts > MAX_ATTEMPTS) {
                if (!avoidColors.contains(newColor)) {
                    return newColor;
                }
            }

        } while (isTooSimilarToExistingColors(newColor, avoidColors, MIN_COLOR_DISTANCE));
        
        return newColor;
    }
    
    private boolean isTooSimilarToExistingColors(String newColor, Set<String> existingColors, int minDistance) {
        for (String existingColor : existingColors) {
            if (getColorDistance(newColor, existingColor) < minDistance) {
                return true;
            }
        }
        return false;
    }

    private int getColorDistance(String c1, String c2) {
        int r1 = Integer.parseInt(c1.substring(1, 3), 16);
        int g1 = Integer.parseInt(c1.substring(3, 5), 16);
        int b1 = Integer.parseInt(c1.substring(5, 7), 16);
        
        int r2 = Integer.parseInt(c2.substring(1, 3), 16);
        int g2 = Integer.parseInt(c2.substring(3, 5), 16);
        int b2 = Integer.parseInt(c2.substring(5, 7), 16);
        
        int rDiff = r1 - r2;
        int gDiff = g1 - g2;
        int bDiff = b1 - b2;

        return (int) Math.sqrt(rDiff * rDiff + gDiff * gDiff + bDiff * bDiff);
    }

    public List<Country> getAllColoredCountries() {
        return countryRepository.findAllWithContinentAndNeighbors();
    }
}
