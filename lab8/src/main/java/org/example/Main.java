package org.example;

import org.example.dao.CityDAO;
import org.example.model.City;
import org.example.utils.BiconnectedComponentsFinder;
import org.example.utils.CityDataImporter;
import org.example.utils.CityGenerator;
import org.example.utils.DistanceCalculator;
import org.example.utils.SisterCityGenerator;

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
            System.out.println("================= DISTANCE CALCULATIONS =================");
            for (int i = 0; i < 5; i++) {
                City city1 = cities.get(random.nextInt(cities.size()));
                City city2 = cities.get(random.nextInt(cities.size()));

                calculateAndPrintDistance(city1, city2);
            }
            System.out.println("========================================================");
            

            System.out.println("\n================ GENERATING FAKE CITIES ================");
            CityGenerator cityGenerator = new CityGenerator(con);
            List<City> fakeCities = cityGenerator.generateCities(5000);
            System.out.println("Generated " + fakeCities.size() + " fake cities.");
            

            List<City> allCities = cityDAO.findAll();

            System.out.println("\n=========== GENERATING SISTER CITY RELATIONS ===========");
            SisterCityGenerator sisterCityGenerator = new SisterCityGenerator(con);
            sisterCityGenerator.generateSisterCityRelationships(allCities, 0.0002);
            

            System.out.println("\n========== FINDING BICONNECTED COMPONENTS =============");
            BiconnectedComponentsFinder biconnFinder = new BiconnectedComponentsFinder(con);
            List<List<City>> biconnectedComponents = biconnFinder.findBiconnectedComponents();
            

            System.out.println("\n=============== BICONNECTED COMPONENTS ================");
            System.out.println("Found " + biconnectedComponents.size() + " biconnected components with 3+ cities");
            
            int count = 0;
            for (List<City> component : biconnectedComponents) {
                if (count < 5) {
                    System.out.println("\nComponent " + (count+1) + " (" + component.size() + " cities):");
                    for (City city : component) {
                        System.out.println("  - " + city.getName() + " (ID: " + city.getId() + ")");
                    }
                }
                count++;
            }
            
            if (count > 5) {
                System.out.println("\n... and " + (count - 5) + " more components.");
            }
            System.out.println("========================================================");
            
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
