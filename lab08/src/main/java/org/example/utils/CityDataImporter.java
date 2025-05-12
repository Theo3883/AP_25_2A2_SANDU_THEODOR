package org.example.utils;

import org.example.dao.CityDAO;
import org.example.dao.ContinentDAO;
import org.example.dao.CountryDAO;
import org.example.model.City;
import org.example.model.Continent;
import org.example.model.Country;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class CityDataImporter {

    public static void importCities(String fileName, Connection con) throws IOException, SQLException {
        CityDAO cityDAO = new CityDAO(con);
        CountryDAO countryDAO = new CountryDAO(con);
        ContinentDAO continentDAO = new ContinentDAO(con);

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(Objects.requireNonNull(CityDataImporter.class.getClassLoader().getResourceAsStream(fileName))))) {
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length < 6) continue;

                String countryName = data[0].trim();
                String continentName = "temp continent";

                List<Continent> continents = continentDAO.findByName(continentName);
                Continent continent;
                if (continents.isEmpty()) {
                    continent = new Continent();
                    continent.setName(continentName);
                    continentDAO.create(continent);
                } else {
                    continent = continents.getFirst();
                }


                List<Country> countries = countryDAO.findByName(countryName);
                Country country;
                if (countries.isEmpty()) {
                    country = new Country();
                    country.setName(countryName);
                    country.setContinentId(continent.getId());
                    countryDAO.create(country);
                } else {
                    country = countries.getFirst();
                }

                City city = new City();
                city.setCountryId(country.getId());
                city.setName(data[1].trim());
                city.setCapital(true);
                city.setLatitude(Double.parseDouble(data[3].trim()));
                city.setLongitude(Double.parseDouble(data[4].trim()));

                cityDAO.create(city);
            }
        }
    }

    public static void clearExistingData(Connection con) throws SQLException {
        clearTable(con, "sister_cities");
        clearTable(con, "cities");
        clearTable(con, "countries");
        clearTable(con, "continents");

        resetAutoIncrement(con, "cities");
        resetAutoIncrement(con, "countries");
        resetAutoIncrement(con, "continents");

        System.out.println("Existing data cleared successfully.");
    }

    private static void clearTable(Connection con, String tableName) throws SQLException {
        String deleteSQL = "DELETE FROM " + tableName;
        try (PreparedStatement stmt = con.prepareStatement(deleteSQL)) {
            stmt.executeUpdate();
            System.out.println("Data cleared from " + tableName + " table.");
        }
    }

    private static void resetAutoIncrement(Connection con, String tableName) throws SQLException {
        String resetSQL = "";
        if (tableName.equals("cities")) {
            resetSQL = "ALTER SEQUENCE cities_id_seq RESTART WITH 1";
        } else if (tableName.equals("countries")) {
            resetSQL = "ALTER SEQUENCE countries_id_seq RESTART WITH 1";
        } else if (tableName.equals("continents")) {
            resetSQL = "ALTER SEQUENCE continents_id_seq RESTART WITH 1";
        }

        try (PreparedStatement stmt = con.prepareStatement(resetSQL)) {
            stmt.executeUpdate();
            System.out.println("Auto-increment reset for " + tableName);
        }
    }
}
