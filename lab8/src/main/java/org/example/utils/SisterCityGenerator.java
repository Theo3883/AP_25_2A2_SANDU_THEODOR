package org.example.utils;

import org.example.dao.SisterCityDAO;
import org.example.model.City;
import org.example.model.SisterCity;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class SisterCityGenerator {
    private final Connection connection;
    private final SisterCityDAO sisterCityDAO;
    private final Random random = new Random();

    public SisterCityGenerator(Connection connection) {
        this.connection = connection;
        this.sisterCityDAO = new SisterCityDAO(connection);
    }

    public void generateSisterCityRelationships(List<City> cities, double probability) throws SQLException {
        System.out.println("Generating sister city relationships with probability " + probability);
        List<SisterCity> sisterCities = new ArrayList<>();
        Set<String> existingPairs = new HashSet<>();
        int totalRelationships = 0;

        for (int i = 0; i < cities.size(); i++) {
            for (int j = i + 1; j < cities.size(); j++) {
                if (random.nextDouble() < probability) {
                    int city1Id = cities.get(i).getId();
                    int city2Id = cities.get(j).getId();

                    //pentru a nu avea duplicate
                    String pair = Math.min(city1Id, city2Id) + "-" + Math.max(city1Id, city2Id);

                    if (!existingPairs.contains(pair)) {
                        SisterCity sisterCity = new SisterCity();
                        sisterCity.setCity1Id(city1Id);
                        sisterCity.setCity2Id(city2Id);
                        sisterCities.add(sisterCity);
                        existingPairs.add(pair);
                        totalRelationships++;


                        if (sisterCities.size() >= 1000) {
                            sisterCityDAO.createBatch(sisterCities);
                            sisterCities.clear();
                            System.out.println("Inserted 1000 sister city relationships");
                        }
                    }
                }
            }

            if (i % 100 == 0 && i > 0) {
                System.out.println("Processed " + i + " cities for sister relationships");
            }
        }

        // Insert any remaining sister cities
        if (!sisterCities.isEmpty()) {
            sisterCityDAO.createBatch(sisterCities);
        }

        System.out.println("Sister city relationship generation complete. Total relationships: " + totalRelationships);
    }
}