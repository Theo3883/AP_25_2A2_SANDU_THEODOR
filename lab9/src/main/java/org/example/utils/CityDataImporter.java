package org.example.utils;

import jakarta.persistence.EntityManager;
import org.example.model.City;
import org.example.model.Continent;
import org.example.model.Country;
import org.example.repository.CityRepository;
import org.example.repository.ContinentRepository;
import org.example.repository.CountryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CityDataImporter {
    private static final Logger logger = LoggerFactory.getLogger(CityDataImporter.class);

    public static void importCities(String fileName, EntityManager entityManager) throws IOException {
        CityRepository cityRepository = new CityRepository(entityManager);
        CountryRepository countryRepository = new CountryRepository(entityManager);
        ContinentRepository continentRepository = new ContinentRepository(entityManager);

        logger.info("Starting to import cities from {}", fileName);

        boolean isActiveTransaction = entityManager.getTransaction().isActive();
        if (!isActiveTransaction) {
            entityManager.getTransaction().begin();
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(Objects.requireNonNull(
                        CityDataImporter.class.getClassLoader().getResourceAsStream(fileName))))) {

            // Skip header
            String line = br.readLine();
            int count = 0;

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length < 6) continue;

                String countryName = data[0].trim();
                String cityName = data[1].trim();
                String continentName = determineContinentForCountry(countryName);

                // Find or create continent
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

                // Find or create country
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

                    // Parse latitude and longitude
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
                        entityManager.flush();
                        entityManager.clear();
                    }
                } catch (Exception e) {
                    logger.error("Error importing city: {}", cityName, e);
                }
            }

            if (!isActiveTransaction) {
                entityManager.getTransaction().commit();
            }

            logger.info("Successfully imported {} cities from {}", count, fileName);

        } catch (Exception e) {
            if (!isActiveTransaction) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error importing cities from {}", fileName, e);
            throw new RuntimeException("Error importing cities", e);
        }
    }

    public static void clearExistingData(EntityManager entityManager) {
        logger.info("Clearing existing data from all tables");

        boolean isActiveTransaction = entityManager.getTransaction().isActive();
        if (!isActiveTransaction) {
            entityManager.getTransaction().begin();
        }

        try {
            entityManager.createQuery("DELETE FROM SisterCity").executeUpdate();
            logger.info("Cleared sister_cities table");

            entityManager.createQuery("DELETE FROM City").executeUpdate();
            logger.info("Cleared cities table");

            entityManager.createQuery("DELETE FROM Country").executeUpdate();
            logger.info("Cleared countries table");

            entityManager.createQuery("DELETE FROM Continent").executeUpdate();
            logger.info("Cleared continents table");

            if (!isActiveTransaction) {
                entityManager.getTransaction().commit();
            }

            logger.info("Successfully cleared all existing data and reset sequences");

        } catch (Exception e) {
            if (!isActiveTransaction) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error clearing existing data", e);
            throw new RuntimeException("Error clearing data", e);
        }
    }

    private static String determineContinentForCountry(String countryName) {
        if (List.of("United States", "Canada", "Mexico", "Costa Rica", "Panama", "Cuba", "Jamaica", "Haiti", "Dominican Republic").contains(countryName)) {
            return "North America";
        } else if (List.of("Brazil", "Argentina", "Chile", "Peru", "Colombia", "Venezuela", "Ecuador", "Bolivia", "Paraguay", "Uruguay").contains(countryName)) {
            return "South America";
        } else if (List.of("United Kingdom", "France", "Germany", "Italy", "Spain", "Portugal", "Netherlands", "Belgium", "Switzerland", "Austria", "Norway", "Sweden", "Finland", "Denmark", "Greece", "Ukraine", "Poland", "Romania", "Russia").contains(countryName)) {
            return "Europe";
        } else if (List.of("China", "Japan", "India", "South Korea", "North Korea", "Vietnam", "Thailand", "Indonesia", "Malaysia", "Philippines", "Pakistan", "Iran", "Iraq", "Saudi Arabia", "Israel", "Turkey").contains(countryName)) {
            return "Asia";
        } else if (List.of("Egypt", "Nigeria", "South Africa", "Morocco", "Kenya", "Ethiopia", "Ghana", "Tanzania", "Sudan", "Algeria", "Tunisia").contains(countryName)) {
            return "Africa";
        } else if (List.of("Australia", "New Zealand", "Papua New Guinea", "Fiji", "Samoa").contains(countryName)) {
            return "Oceania";
        } else {
            return "Other";
        }
    }

    private static String generateCountryCode(String countryName) {
        if (countryName.length() < 2) {
            return countryName.toUpperCase() + "X";
        }

        return countryName.substring(0, 2).toUpperCase();
    }
}