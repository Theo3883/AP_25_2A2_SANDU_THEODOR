package org.example.utils;

import jakarta.persistence.EntityManager;
import org.example.model.City;
import org.example.model.SisterCity;
import org.example.repository.SisterCityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class SisterCityGenerator {
    private final EntityManager entityManager;
    private final SisterCityRepository sisterCityRepository;
    private final Random random = new Random();
    private final Logger logger = LoggerFactory.getLogger(SisterCityGenerator.class);

    public SisterCityGenerator(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.sisterCityRepository = new SisterCityRepository(entityManager);
    }

    public void generateSisterCityRelationships(List<City> cities, double probability) {
        logger.info("Generating sister city relationships with probability {}", probability);
        long startTime = System.currentTimeMillis();

        List<SisterCity> sisterCities = new ArrayList<>();
        Set<String> existingPairs = new HashSet<>();
        int totalRelationships = 0;

        boolean isActiveTransaction = entityManager.getTransaction().isActive();
        if (!isActiveTransaction) {
            entityManager.getTransaction().begin();
        }

        try {
            for (int i = 0; i < cities.size(); i++) {
                City city1 = cities.get(i);

                for (int j = i + 1; j < cities.size(); j++) {
                    if (random.nextDouble() < probability) {
                        City city2 = cities.get(j);

                        int city1Id = city1.getId();
                        int city2Id = city2.getId();
                        String pair = Math.min(city1Id, city2Id) + "-" + Math.max(city1Id, city2Id);

                        if (!existingPairs.contains(pair)) {
                            SisterCity sisterCity = new SisterCity();

                            // Ensure smaller ID is always city1 for consistency
                            if (city1Id < city2Id) {
                                sisterCity.setCity1(city1);
                                sisterCity.setCity2(city2);
                            } else {
                                sisterCity.setCity1(city2);
                                sisterCity.setCity2(city1);
                            }

                            sisterCities.add(sisterCity);
                            existingPairs.add(pair);
                            totalRelationships++;

                            // Process in batches of 1000
                            if (sisterCities.size() >= 1000) {
                                sisterCityRepository.bulkSave(sisterCities);
                                sisterCities.clear();

                                // Clear persistence context to avoid memory issues
                                entityManager.clear();

                                logger.info("Inserted 1000 sister city relationships (total so far: {})",
                                        totalRelationships);
                            }
                        }
                    }
                }

                if (i % 100 == 0 && i > 0) {
                    logger.info("Processed {} cities for sister relationships", i);
                }
            }

            if (!sisterCities.isEmpty()) {
                sisterCityRepository.bulkSave(sisterCities);
            }

            if (!isActiveTransaction) {
                entityManager.getTransaction().commit();
            }

            long endTime = System.currentTimeMillis();
            logger.info("Sister city relationship generation complete. Total relationships: {} in {}ms",
                    totalRelationships, endTime - startTime);

        } catch (Exception e) {
            if (!isActiveTransaction) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error generating sister city relationships", e);
            throw new RuntimeException("Error generating sister city relationships", e);
        }
    }

    public void generateSisterCitiesFromCapitals(double probability) {
        logger.info("Generating sister city relationships between capitals with probability {}", probability);
        long startTime = System.currentTimeMillis();

        String jpql = "SELECT c FROM City c WHERE c.isCapital = true";
        List<City> capitals = entityManager.createQuery(jpql, City.class).getResultList();

        if (capitals.size() < 2) {
            logger.info("Not enough capitals found to generate sister city relationships");
            return;
        }

        logger.info("Found {} capital cities to process", capitals.size());
        generateSisterCityRelationships(capitals, probability);

        long endTime = System.currentTimeMillis();
        logger.info("Capital sister city relationship generation completed in {}ms", endTime - startTime);
    }
}