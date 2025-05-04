package org.example;

import jakarta.persistence.EntityManager;
import org.example.model.City;
import org.example.repository.CityRepository;
import org.example.utils.CityDataImporter;
import org.example.utils.CityGenerator;
import org.example.utils.JPAUtil;
import org.example.utils.SisterCityGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        List<City> capitals = null;

        try {

            EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();

            CityDataImporter.clearExistingData(entityManager);
            CityDataImporter.importCities("countries.csv", entityManager);

            CityGenerator cityGenerator = new CityGenerator(entityManager);
            List<City> generatedCities = cityGenerator.generateCities(100);


            SisterCityGenerator sisterCityGenerator = new SisterCityGenerator(entityManager);
            sisterCityGenerator.generateSisterCitiesFromCapitals(0.1);


            CityRepository cityRepository = new CityRepository(entityManager);

            //query
            capitals = cityRepository.findCapitals();


            JPAUtil.close();
            logger.info("Application completed successfully");


            if (capitals != null && !capitals.isEmpty()) {
                capitals.stream().limit(5).forEach(city ->
                        logger.info("Capital: {} ({}), Location: {}, {}",
                                city.getName(),
                                city.getCountry().getName(),
                                city.getLatitude(),
                                city.getLongitude())
                );
            }

        } catch (IOException e) {
            logger.error("Error during data import: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
        }
    }
}