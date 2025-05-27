package com.backend.demo.bootstrap;

import com.backend.demo.model.Continent;
import com.backend.demo.model.Country;
import com.backend.demo.repository.ContinentRepository;
import com.backend.demo.repository.CountryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private final CountryRepository countryRepository;
    private final ContinentRepository continentRepository;

    @Autowired
    public DataInitializer(CountryRepository countryRepository, ContinentRepository continentRepository) {
        this.countryRepository = countryRepository;
        this.continentRepository = continentRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (countryRepository.count() > 0) {
            logger.info("Database already initialized with {} countries, skipping initialization", countryRepository.count());
            return;
        }
        
        logger.info("Initializing database with countries and their relationships");
        
        Continent europe = new Continent();
        europe.setName("Europe");
        continentRepository.save(europe);

        Map<String, Country> countries = new HashMap<>();
        
        String[] countryNames = {"France", "Germany", "Spain", "Italy", "Portugal", 
                                 "Belgium", "Netherlands", "Switzerland", "Austria", "Poland"};
        
        for (String name : countryNames) {
            Country country = new Country();
            country.setName(name);
            country.setContinent(europe);
            country.setCode(generateCountryCode(name));
            countries.put(name, country);
        }
        
        countryRepository.saveAll(countries.values());
        
        defineNeighbors(countries.get("France"), 
                Arrays.asList(countries.get("Spain"), countries.get("Germany"), 
                              countries.get("Italy"), countries.get("Belgium"), 
                              countries.get("Switzerland")));
        
        defineNeighbors(countries.get("Germany"), 
                Arrays.asList(countries.get("France"), countries.get("Netherlands"), 
                              countries.get("Belgium"), countries.get("Poland"), 
                              countries.get("Austria"), countries.get("Switzerland")));
        
        defineNeighbors(countries.get("Spain"), 
                Arrays.asList(countries.get("France"), countries.get("Portugal")));
        
        defineNeighbors(countries.get("Italy"), 
                Arrays.asList(countries.get("France"), countries.get("Switzerland"), 
                              countries.get("Austria")));
        
        defineNeighbors(countries.get("Portugal"), 
                Collections.singletonList(countries.get("Spain")));
        
        defineNeighbors(countries.get("Belgium"), 
                Arrays.asList(countries.get("France"), countries.get("Germany"), 
                              countries.get("Netherlands")));
        
        defineNeighbors(countries.get("Netherlands"), 
                Arrays.asList(countries.get("Germany"), countries.get("Belgium")));
        
        defineNeighbors(countries.get("Switzerland"), 
                Arrays.asList(countries.get("France"), countries.get("Germany"), 
                              countries.get("Italy"), countries.get("Austria")));
        
        defineNeighbors(countries.get("Austria"), 
                Arrays.asList(countries.get("Germany"), countries.get("Italy"), 
                              countries.get("Switzerland")));
        
        defineNeighbors(countries.get("Poland"), 
                Collections.singletonList(countries.get("Germany")));
        
        countryRepository.saveAll(countries.values());
        
        logger.info("Initialized database with {} countries", countryRepository.count());
    }
    
    private void defineNeighbors(Country country, List<Country> neighbors) {
        country.setNeighbors(neighbors);
    }
    
    private String generateCountryCode(String countryName) {
        if (countryName.length() < 2) {
            return countryName.toUpperCase() + "X";
        }
        return countryName.substring(0, 2).toUpperCase();
    }
}
