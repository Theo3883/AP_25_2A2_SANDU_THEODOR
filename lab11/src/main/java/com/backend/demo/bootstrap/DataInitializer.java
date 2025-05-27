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
    private final Random random = new Random();

    // Probability constants for country connections
    private static final double SAME_CONTINENT_CONNECTION_PROBABILITY = 0.1; // Reduced from 0.4
    private static final double DIFFERENT_CONTINENT_CONNECTION_PROBABILITY = 0.01; // Reduced from 0.05

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
            
            // Update any countries without neighbors
            establishMissingNeighbors();
            return;
        }
        
        logger.info("Initializing database with countries and their relationships");
        
        // Create continents
        Map<String, Continent> continents = createContinents();
        
        // Create countries for each continent
        Map<String, Country> countries = createCountriesForContinents(continents);
        
        // Save all countries first
        countryRepository.saveAll(countries.values());
        
        // Establish neighbor relationships
        establishNeighborRelationships(countries);
        
        // Save the updated relationships
        countryRepository.saveAll(countries.values());
        
        logger.info("Initialized database with {} countries across {} continents", 
                countries.size(), continents.size());
    }
    
    private Map<String, Continent> createContinents() {
        Map<String, Continent> continents = new HashMap<>();
        
        String[] continentNames = {"Europe", "Asia", "Africa", "North America", "South America", "Oceania"};
        
        for (String name : continentNames) {
            Continent continent = new Continent();
            continent.setName(name);
            continentRepository.save(continent);
            continents.put(name, continent);
            logger.debug("Created continent: {}", name);
        }
        
        return continents;
    }
    
    private Map<String, Country> createCountriesForContinents(Map<String, Continent> continents) {
        Map<String, Country> allCountries = new HashMap<>();
        
        // Europe countries
        String[] europeCountries = {
            "France", "Germany", "Spain", "Italy", "Portugal", "Belgium", "Netherlands", 
            "Switzerland", "Austria", "Poland", "Sweden", "Norway", "Finland", "Denmark", 
            "Greece", "Romania", "Bulgaria", "Croatia", "Hungary", "Czech Republic"
        };
        createCountriesForContinent(continents.get("Europe"), europeCountries, allCountries);
        
        // Asia countries
        String[] asiaCountries = {
            "China", "Japan", "India", "South Korea", "Vietnam", "Thailand", "Malaysia", 
            "Indonesia", "Philippines", "Pakistan", "Iran", "Iraq", "Saudi Arabia", "Turkey"
        };
        createCountriesForContinent(continents.get("Asia"), asiaCountries, allCountries);
        
        // Africa countries
        String[] africaCountries = {
            "Egypt", "Nigeria", "South Africa", "Morocco", "Kenya", "Ethiopia", "Ghana", 
            "Tanzania", "Algeria", "Tunisia", "Uganda", "Zimbabwe", "Cameroon"
        };
        createCountriesForContinent(continents.get("Africa"), africaCountries, allCountries);
        
        // North America countries
        String[] northAmericaCountries = {
            "United States", "Canada", "Mexico", "Cuba", "Jamaica", "Panama", "Costa Rica", 
            "Dominican Republic", "Haiti", "Guatemala", "El Salvador", "Honduras"
        };
        createCountriesForContinent(continents.get("North America"), northAmericaCountries, allCountries);
        
        // South America countries
        String[] southAmericaCountries = {
            "Brazil", "Argentina", "Colombia", "Chile", "Peru", "Venezuela", "Uruguay", 
            "Ecuador", "Bolivia", "Paraguay"
        };
        createCountriesForContinent(continents.get("South America"), southAmericaCountries, allCountries);
        
        // Oceania countries
        String[] oceaniaCountries = {
            "Australia", "New Zealand", "Fiji", "Papua New Guinea", "Solomon Islands", 
            "Vanuatu", "Samoa", "Tonga"
        };
        createCountriesForContinent(continents.get("Oceania"), oceaniaCountries, allCountries);
        
        return allCountries;
    }
    
    private void createCountriesForContinent(Continent continent, String[] countryNames, Map<String, Country> allCountries) {
        for (String name : countryNames) {
            Country country = new Country();
            country.setName(name);
            country.setContinent(continent);
            country.setCode(generateCountryCode(name));
            allCountries.put(name, country);
        }
    }
    
    private void establishNeighborRelationships(Map<String, Country> countries) {
        // Group countries by continent
        Map<Continent, List<Country>> countriesByContinent = new HashMap<>();
        
        for (Country country : countries.values()) {
            countriesByContinent.computeIfAbsent(country.getContinent(), k -> new ArrayList<>()).add(country);
        }
        
        // For each continent, establish relationships between countries with much lower probability
        for (List<Country> continentCountries : countriesByContinent.values()) {
            for (Country country : continentCountries) {
                List<Country> neighbors = new ArrayList<>();
                
                // Consider each potential neighbor in the same continent with reduced probability
                for (Country potentialNeighbor : continentCountries) {
                    if (!potentialNeighbor.equals(country) && 
                        random.nextDouble() < SAME_CONTINENT_CONNECTION_PROBABILITY) {
                        neighbors.add(potentialNeighbor);
                    }
                }
                
                // Consider very few connections to countries from other continents
                for (List<Country> otherContinentCountries : countriesByContinent.values()) {
                    if (otherContinentCountries != continentCountries) { // Different continent
                        for (Country potentialNeighbor : otherContinentCountries) {
                            if (random.nextDouble() < DIFFERENT_CONTINENT_CONNECTION_PROBABILITY) {
                                neighbors.add(potentialNeighbor);
                            }
                        }
                    }
                }
                
                // Set neighbors for this country
                // Only ensure minimal connectivity for isolated countries
                if (neighbors.isEmpty() && continentCountries.size() > 1 && random.nextDouble() < 0.3) {
                    // 70% chance to remain isolated, only 30% chance to get a single neighbor
                    Country randomNeighbor = continentCountries.get(random.nextInt(continentCountries.size()));
                    if (!randomNeighbor.equals(country)) {
                        neighbors.add(randomNeighbor);
                    }
                }
                
                country.setNeighbors(neighbors);
                logger.debug("Set {} neighbors for {}", neighbors.size(), country.getName());
            }
        }
    }
    
    @Transactional
    public void establishMissingNeighbors() {
        List<Country> countriesWithoutNeighbors = countryRepository.findCountriesWithoutNeighbors();
        
        if (!countriesWithoutNeighbors.isEmpty()) {
            logger.info("Found {} countries without neighbors. Adding neighbor relationships to some of them.", 
                    countriesWithoutNeighbors.size());
            
            List<Country> allCountries = countryRepository.findAll();
            Map<String, Country> countries = new HashMap<>();
            for (Country country : allCountries) {
                countries.put(country.getName(), country);
            }
            
            // Group countries by continent
            Map<Continent, List<Country>> countriesByContinent = new HashMap<>();
            for (Country country : allCountries) {
                countriesByContinent.computeIfAbsent(country.getContinent(), k -> new ArrayList<>()).add(country);
            }
            
            for (Country country : countriesWithoutNeighbors) {
                // 70% chance to leave country isolated
                if (random.nextDouble() < 0.7) {
                    logger.debug("Keeping {} isolated intentionally", country.getName());
                    continue;
                }
                
                List<Country> continentCountries = countriesByContinent.get(country.getContinent());
                List<Country> neighbors = new ArrayList<>();
                
                // Add very few neighbors from the same continent
                for (Country potentialNeighbor : continentCountries) {
                    if (!potentialNeighbor.equals(country) && 
                        random.nextDouble() < SAME_CONTINENT_CONNECTION_PROBABILITY) {
                        neighbors.add(potentialNeighbor);
                    }
                }
                
                // Add almost no neighbors from other continents
                for (List<Country> otherContinentCountries : countriesByContinent.values()) {
                    if (otherContinentCountries != continentCountries) {
                        for (Country potentialNeighbor : otherContinentCountries) {
                            if (random.nextDouble() < DIFFERENT_CONTINENT_CONNECTION_PROBABILITY) {
                                neighbors.add(potentialNeighbor);
                            }
                        }
                    }
                }
                
                // Only give a neighbor if we have none and with low probability
                if (neighbors.isEmpty() && continentCountries.size() > 1 && random.nextDouble() < 0.3) {
                    int randomIndex;
                    Country randomNeighbor;
                    do {
                        randomIndex = random.nextInt(continentCountries.size());
                        randomNeighbor = continentCountries.get(randomIndex);
                    } while (randomNeighbor.equals(country));
                    
                    neighbors.add(randomNeighbor);
                }
                
                country.setNeighbors(neighbors);
                countryRepository.save(country);
                logger.debug("Added {} neighbors to {}", neighbors.size(), country.getName());
            }
            
            logger.info("Completed adding neighbor relationships for some countries without neighbors");
        }
    }
    
    private String generateCountryCode(String countryName) {
        if (countryName.length() < 2) {
            return countryName.toUpperCase() + "X";
        }
        return countryName.substring(0, 2).toUpperCase();
    }
}
