package com.backend.demo.utils;

import com.backend.demo.model.City;
import com.backend.demo.model.Continent;
import com.backend.demo.model.Country;
import com.backend.demo.repository.CityRepository;
import com.backend.demo.repository.ContinentRepository;
import com.backend.demo.repository.CountryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

@Component
public class CityDataImporter {
    private static final Logger logger = LoggerFactory.getLogger(CityDataImporter.class);

    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;
    private final ContinentRepository continentRepository;

    public CityDataImporter(CityRepository cityRepository, CountryRepository countryRepository,
            ContinentRepository continentRepository) {
        this.cityRepository = cityRepository;
        this.countryRepository = countryRepository;
        this.continentRepository = continentRepository;
    }

    @Transactional
    public void importCities(String fileName) throws IOException {
        logger.info("Starting to import cities from {}", fileName);

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new ClassPathResource(fileName).getInputStream()))) {

            String line = br.readLine();
            int count = 0;

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length < 6)
                    continue;

                String countryName = data[0].trim();
                String cityName = data[1].trim();
                String continentName = determineContinentForCountry(countryName);

                Optional<Continent> continentOpt = continentRepository.findByName(continentName);
                Continent continent;

                if (continentOpt.isEmpty()) {
                    continent = new Continent();
                    continent.setName(continentName);
                    continent = continentRepository.save(continent);
                    logger.debug("Created new continent: {}", continentName);
                } else {
                    continent = continentOpt.get();
                }

                Optional<Country> countryOpt = countryRepository.findByName(countryName);
                Country country;

                if (countryOpt.isEmpty()) {
                    country = new Country();
                    country.setName(countryName);
                    country.setContinent(continent);
                    country.setCode(generateCountryCode(countryName));
                    country = countryRepository.save(country);
                    logger.debug("Created new country: {}", countryName);
                } else {
                    country = countryOpt.get();
                }

                // Create city
                try {
                    City city = new City();
                    city.setName(cityName);
                    city.setCountry(country);
                    city.setIsCapital(true);

                    try {
                        city.setLatitude(Double.parseDouble(data[2].trim()));
                        city.setLongitude(Double.parseDouble(data[3].trim()));
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid coordinates for {}: {}, {}", cityName, data[2], data[3]);
                        city.setLatitude(0.0);
                        city.setLongitude(0.0);
                    }

                    cityRepository.save(city);
                    count++;

                    if (count % 50 == 0) {
                        logger.info("Imported {} cities so far", count);
                    }
                } catch (Exception e) {
                    logger.error("Error importing city: {}", cityName, e);
                }
            }

            logger.info("Successfully imported {} cities from {}", count, fileName);

        } catch (Exception e) {
            logger.error("Error importing cities from {}", fileName, e);
            throw new RuntimeException("Error importing cities", e);
        }
    }

    @Transactional
    public void clearExistingData() {
        logger.info("Clearing existing data from all tables");

        try {
            logger.info("Clearing cities table");
            cityRepository.deleteAll();

            logger.info("Clearing countries table");
            countryRepository.deleteAll();

            logger.info("Clearing continents table");
            continentRepository.deleteAll();

            logger.info("Successfully cleared all existing data");
        } catch (Exception e) {
            logger.error("Error clearing existing data", e);
            throw new RuntimeException("Error clearing data", e);
        }
    }

    private String determineContinentForCountry(String countryName) {
        if (List.of("United States", "Canada", "Mexico", "Costa Rica", "Panama", "Cuba", "Jamaica", "Haiti",
                "Dominican Republic").contains(countryName)) {
            return "North America";
        } else if (List.of("Brazil", "Argentina", "Chile", "Peru", "Colombia", "Venezuela", "Ecuador", "Bolivia",
                "Paraguay", "Uruguay").contains(countryName)) {
            return "South America";
        } else if (List.of("United Kingdom", "France", "Germany", "Italy", "Spain", "Portugal", "Netherlands",
                "Belgium", "Switzerland", "Austria", "Norway", "Sweden", "Finland", "Denmark", "Greece", "Ukraine",
                "Poland", "Romania", "Russia").contains(countryName)) {
            return "Europe";
        } else if (List
                .of("China", "Japan", "India", "South Korea", "North Korea", "Vietnam", "Thailand", "Indonesia",
                        "Malaysia", "Philippines", "Pakistan", "Iran", "Iraq", "Saudi Arabia", "Israel", "Turkey")
                .contains(countryName)) {
            return "Asia";
        } else if (List.of("Egypt", "Nigeria", "South Africa", "Morocco", "Kenya", "Ethiopia", "Ghana", "Tanzania",
                "Sudan", "Algeria", "Tunisia").contains(countryName)) {
            return "Africa";
        } else if (List.of("Australia", "New Zealand", "Papua New Guinea", "Fiji", "Samoa").contains(countryName)) {
            return "Oceania";
        } else {
            return "Other";
        }
    }

    private String generateCountryCode(String countryName) {
        if (countryName.length() < 2) {
            return countryName.toUpperCase() + "X";
        }

        return countryName.substring(0, 2).toUpperCase();
    }
}