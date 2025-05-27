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

    private static final double SAME_CONTINENT_CONNECTION_PROBABILITY = 0.1;
    private static final double DIFFERENT_CONTINENT_CONNECTION_PROBABILITY = 0.01;

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
            establishMissingNeighbors();
            return;
        }
        
        logger.info("Initializing database with countries and their relationships");
        Map<String, Continent> continents = createContinents();
        Map<String, Country> countries = createCountriesForContinents(continents);
        countryRepository.saveAll(countries.values());
        establishNeighborRelationships(countries);
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

        String[] europeCountries = {
            "France", "Germany", "Spain", "Italy", "Portugal", "Belgium", "Netherlands", 
            "Switzerland", "Austria", "Poland", "Sweden", "Norway", "Finland", "Denmark", 
            "Greece", "Romania", "Bulgaria", "Croatia", "Hungary", "Czech Republic"
        };
        createCountriesForContinent(continents.get("Europe"), europeCountries, allCountries);

        String[] asiaCountries = {
            "China", "Japan", "India", "South Korea", "Vietnam", "Thailand", "Malaysia", 
            "Indonesia", "Philippines", "Pakistan", "Iran", "Iraq", "Saudi Arabia", "Turkey"
        };
        createCountriesForContinent(continents.get("Asia"), asiaCountries, allCountries);

        String[] africaCountries = {
            "Egypt", "Nigeria", "South Africa", "Morocco", "Kenya", "Ethiopia", "Ghana", 
            "Tanzania", "Algeria", "Tunisia", "Uganda", "Zimbabwe", "Cameroon"
        };
        createCountriesForContinent(continents.get("Africa"), africaCountries, allCountries);

        String[] northAmericaCountries = {
            "United States", "Canada", "Mexico", "Cuba", "Jamaica", "Panama", "Costa Rica", 
            "Dominican Republic", "Haiti", "Guatemala", "El Salvador", "Honduras"
        };
        createCountriesForContinent(continents.get("North America"), northAmericaCountries, allCountries);

        String[] southAmericaCountries = {
            "Brazil", "Argentina", "Colombia", "Chile", "Peru", "Venezuela", "Uruguay", 
            "Ecuador", "Bolivia", "Paraguay"
        };
        createCountriesForContinent(continents.get("South America"), southAmericaCountries, allCountries);

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
        Map<Continent, List<Country>> countriesByContinent = new HashMap<>();
        
        for (Country country : countries.values()) {
            countriesByContinent.computeIfAbsent(country.getContinent(), k -> new ArrayList<>()).add(country);
        }

        for (List<Country> continentCountries : countriesByContinent.values()) {
            for (Country country : continentCountries) {
                List<Country> neighbors = new ArrayList<>();
                for (Country potentialNeighbor : continentCountries) {
                    if (!potentialNeighbor.equals(country) && 
                        random.nextDouble() < SAME_CONTINENT_CONNECTION_PROBABILITY) {
                        neighbors.add(potentialNeighbor);
                    }
                }

                for (List<Country> otherContinentCountries : countriesByContinent.values()) {
                    if (otherContinentCountries != continentCountries) {
                        for (Country potentialNeighbor : otherContinentCountries) {
                            if (random.nextDouble() < DIFFERENT_CONTINENT_CONNECTION_PROBABILITY) {
                                neighbors.add(potentialNeighbor);
                            }
                        }
                    }
                }

                if (neighbors.isEmpty() && continentCountries.size() > 1 && random.nextDouble() < 0.3) {
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

            Map<Continent, List<Country>> countriesByContinent = new HashMap<>();
            for (Country country : allCountries) {
                countriesByContinent.computeIfAbsent(country.getContinent(), k -> new ArrayList<>()).add(country);
            }
            
            for (Country country : countriesWithoutNeighbors) {
                if (random.nextDouble() < 0.7) {
                    logger.debug("Keeping {} isolated intentionally", country.getName());
                    continue;
                }
                
                List<Country> continentCountries = countriesByContinent.get(country.getContinent());
                List<Country> neighbors = new ArrayList<>();
                for (Country potentialNeighbor : continentCountries) {
                    if (!potentialNeighbor.equals(country) && 
                        random.nextDouble() < SAME_CONTINENT_CONNECTION_PROBABILITY) {
                        neighbors.add(potentialNeighbor);
                    }
                }

                for (List<Country> otherContinentCountries : countriesByContinent.values()) {
                    if (otherContinentCountries != continentCountries) {
                        for (Country potentialNeighbor : otherContinentCountries) {
                            if (random.nextDouble() < DIFFERENT_CONTINENT_CONNECTION_PROBABILITY) {
                                neighbors.add(potentialNeighbor);
                            }
                        }
                    }
                }

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
