
package org.example.utils;

import jakarta.persistence.EntityManager;
import org.example.model.City;
import org.example.model.Country;
import org.example.repository.CityRepository;
import org.example.repository.CountryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CityGenerator {
    private final EntityManager entityManager;
    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;
    private final Random random = new Random();
    private final Logger logger = LoggerFactory.getLogger(CityGenerator.class);

    private final String[] prefixes = {"North", "South", "East", "West", "New", "Old", "Upper", "Lower", "Central", "Great"};
    private final String[] roots = {"Spring", "Oak", "Pine", "Maple", "River", "Lake", "Hill", "Valley", "Mountain", "Field",
            "Forest", "Creek", "Bridge", "Glen", "Haven", "Harbor", "Port", "Bay", "Cove", "Beach"};
    private final String[] suffixes = {"ville", "town", "burg", "city", "field", "ford", "port", "shire", "berg", "ton",
            "land", "wood", "dale", "view", "side", "ridge", "haven", "harbor", "bury", "crest"};

    public CityGenerator(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.cityRepository = new CityRepository(entityManager);
        this.countryRepository = new CountryRepository(entityManager);
    }

    public List<City> generateCities(int count) {
        List<City> cities = new ArrayList<>();
        List<Country> countries = countryRepository.findAll();

        if (countries.isEmpty()) {
            logger.error("No countries found in the database");
            throw new IllegalStateException("No countries found in the database");
        }

        logger.info("Starting to generate {} cities...", count);
        long startTime = System.currentTimeMillis();

        boolean isActiveTransaction = entityManager.getTransaction().isActive();
        if (!isActiveTransaction) {
            entityManager.getTransaction().begin();
        }

        try {
            List<City> batch = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                City city = new City();
                city.setName(generateRandomCityName());
                city.setCountry(countries.get(random.nextInt(countries.size())));
                city.setIsCapital(false);
                city.setLatitude(random.nextDouble() * 180 - 90);    // -90 to 90
                city.setLongitude(random.nextDouble() * 360 - 180);  // -180 to 180

                batch.add(city);
                cities.add(city);

                // Process in batches of 1000
                if (batch.size() >= 1000) {
                    cityRepository.bulkSave(batch);
                    batch.clear();

                    // Clear persistence context to avoid memory issues
                    entityManager.clear();

                    logger.info("Generated {} cities", i + 1);
                }
            }


            if (!batch.isEmpty()) {
                cityRepository.bulkSave(batch);
            }

            if (!isActiveTransaction) {
                entityManager.getTransaction().commit();
            }

            long endTime = System.currentTimeMillis();
            logger.info("Generated {} cities successfully in {}ms", count, endTime - startTime);

        } catch (Exception e) {
            if (!isActiveTransaction) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error generating cities", e);
            throw new RuntimeException("Error generating cities", e);
        }

        return cities;
    }

    private String generateRandomCityName() {
        String prefix = random.nextDouble() < 0.3 ? prefixes[random.nextInt(prefixes.length)] + " " : "";
        String root = roots[random.nextInt(roots.length)];
        String suffix = suffixes[random.nextInt(suffixes.length)];
        return prefix + root + suffix;
    }
}