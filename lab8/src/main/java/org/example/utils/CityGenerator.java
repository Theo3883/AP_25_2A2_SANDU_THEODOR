package org.example.utils;

import org.example.dao.CityDAO;
import org.example.dao.CountryDAO;
import org.example.model.City;
import org.example.model.Country;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CityGenerator {
    private final Connection connection;
    private final CityDAO cityDAO;
    private final CountryDAO countryDAO;
    private final Random random = new Random();
    private final String[] prefixes = {"North", "South", "East", "West", "New", "Old", "Upper", "Lower", "Central", "Great"};
    private final String[] roots = {"Spring", "Oak", "Pine", "Maple", "River", "Lake", "Hill", "Valley", "Mountain", "Field",
            "Forest", "Creek", "Bridge", "Glen", "Haven", "Harbor", "Port", "Bay", "Cove", "Beach"};
    private final String[] suffixes = {"ville", "town", "burg", "city", "field", "ford", "port", "shire", "berg", "ton",
            "land", "wood", "dale", "view", "side", "ridge", "haven", "harbor", "bury", "crest"};

    public CityGenerator(Connection connection) {
        this.connection = connection;
        this.cityDAO = new CityDAO(connection);
        this.countryDAO = new CountryDAO(connection);
    }

    public List<City> generateCities(int count) throws SQLException {
        List<City> cities = new ArrayList<>();
        List<Country> countries = countryDAO.findAll();
        if (countries.isEmpty()) {
            throw new IllegalStateException("No countries found in the database");
        }

        System.out.println("Starting to generate " + count + " cities...");
        try {
            connection.setAutoCommit(false);

            for (int i = 0; i < count; i++) {
                City city = new City();
                city.setName(generateRandomCityName());
                city.setCountryId(countries.get(random.nextInt(countries.size())).getId());
                city.setCapital(false);
                city.setLatitude(random.nextDouble() * 180 - 90);    // -90 to 90
                city.setLongitude(random.nextDouble() * 360 - 180);  // -180 to 180
                cityDAO.create(city);
                cities.add(city);

                if (i % 1000 == 0) {
                    connection.commit();
                    System.out.println("Generated " + i + " cities");
                }
            }

            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            connection.rollback();
            connection.setAutoCommit(true);
            throw e;
        }

        System.out.println("Generated " + count + " cities successfully.");
        return cities;
    }

    private String generateRandomCityName() {
        String prefix = random.nextDouble() < 0.3 ? prefixes[random.nextInt(prefixes.length)] + " " : "";
        String root = roots[random.nextInt(roots.length)];
        String suffix = suffixes[random.nextInt(suffixes.length)];
        return prefix + root + suffix;
    }
}