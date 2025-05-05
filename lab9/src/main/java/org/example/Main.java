package org.example;

import jakarta.persistence.EntityManager;
import org.example.dao.CityDAO;
import org.example.factory.DAOFactory;
import org.example.model.City;
import org.example.repository.CityRepository;
import org.example.utils.CityDataImporter;
import org.example.utils.CityGenerator;
import org.example.utils.CitySolver;
import org.example.utils.JPAUtil;
import org.example.utils.SisterCityGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            EntityManager entityManager = setupEntityManager();
            clearAndImportCities(entityManager);

            List<City> generatedCities = generateCities(entityManager);
            generateSisterCities(entityManager);

            CityDAO cityDAO = DAOFactory.getCityDAO();
            updateCitiesWithRandomPopulation(cityDAO);

            List<City> capitals = queryCapitals(entityManager);

            displayCapitals(capitals);
            useConstraintSolver();
            closeEntityManager();
        } catch (IOException e) {
            logger.error("Error during data import: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
        }
    }

    private static EntityManager setupEntityManager() {
        logger.info("Setting up JPA EntityManager");
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        logger.info("EntityManager created successfully");
        return entityManager;
    }

    private static void clearAndImportCities(EntityManager entityManager) throws IOException {
        logger.info("Clearing and importing cities");
        CityDataImporter.clearExistingData(entityManager);
        CityDataImporter.importCities("countries.csv", entityManager);
        logger.info("City data import completed");
    }

    private static List<City> generateCities(EntityManager entityManager) {
        logger.info("Generating cities");
        CityGenerator cityGenerator = new CityGenerator(entityManager);
        return cityGenerator.generateCities(100);
    }

    private static void generateSisterCities(EntityManager entityManager) {
        logger.info("Generating sister cities");
        SisterCityGenerator sisterCityGenerator = new SisterCityGenerator(entityManager);
        sisterCityGenerator.generateSisterCitiesFromCapitals(0.1);
        logger.info("Sister cities generated");
    }

    private static void updateCitiesWithRandomPopulation(CityDAO cityDAO) {
        logger.info("Updating cities with random population data");
        List<City> cities = cityDAO.findAll();
        Random random = new Random();

        for (City city : cities) {
            if (city.getPopulation() == null) {
                city.setPopulation(city.getIsCapital() ? random.nextInt(5000000) + 500000 : random.nextInt(1000000) + 10000);
                cityDAO.update(city);
            }
        }
    }

    private static List<City> queryCapitals(EntityManager entityManager) {
        logger.info("Querying capitals");
        CityRepository cityRepository = new CityRepository(entityManager);
        return cityRepository.findCapitals();
    }

    private static void displayCapitals(List<City> capitals) {
        logger.info("Displaying capitals");
        if (capitals != null && !capitals.isEmpty()) {
            capitals.stream().limit(5).forEach(city ->
                    logger.info("Capital: {} ({}), Location: {}, {}",
                            city.getName(),
                            city.getCountry().getName(),
                            city.getLatitude(),
                            city.getLongitude())
            );
        }
    }

    private static void useConstraintSolver() {
        logger.info("Using constraint solver");
        CitySolver solver = new CitySolver();
        List<City> result = solver.findCitiesByConstraints('B', 1000000, 5000000);
        logger.info("Found {} cities meeting the criteria:", result.size());
        for (City city : result) {
            logger.info("{} ({}): Population {}",
                    city.getName(),
                    city.getCountry().getName(),
                    city.getPopulation());
        }
    }

    private static void closeEntityManager() {
        logger.info("Closing EntityManager");
        JPAUtil.close();
        logger.info("Application completed successfully");
    }
}
