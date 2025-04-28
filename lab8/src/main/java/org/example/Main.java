package org.example;

import org.example.dao.CityDAO;
import org.example.model.City;
import org.example.utils.CityDataImporter;
import org.example.utils.DistanceCalculator;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

public class Main {

    public static void main(String[] args) {
        try (Connection con = Database.getConnection()) {
            CityDataImporter.clearExistingData(con);
            CityDataImporter.importCities("countries.csv", con);

            CityDAO cityDAO = new CityDAO(con);
            List<City> cities = cityDAO.findAll();

            Random random = new Random();
            System.out.println("=======================================================================");
            System.out.print("\n\n\n\n\n\n");
            for (int i = 0; i < 5; i++) {
                City city1 = cities.get(random.nextInt(cities.size()));
                City city2 = cities.get(random.nextInt(cities.size()));

                calculateAndPrintDistance(city1, city2);
            }
            System.out.print("\n\n\n\n\n\n");
            System.out.println("=======================================================================");

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            Database.closeConnection();
        }
    }

    private static void calculateAndPrintDistance(City city1, City city2) {
        if (city1 != null && city2 != null && !city1.equals(city2)) {
            double distance = DistanceCalculator.calculateDistance(
                    city1.getLatitude(), city1.getLongitude(),
                    city2.getLatitude(), city2.getLongitude()
            );
            System.out.println("Distance between " + city1.getName() + " and " + city2.getName() + ": " + distance + " km");
        } else {
            System.out.println("Invalid city pair or cities are the same.");
        }
    }
}
