package org.example.utils;

import org.example.dao.CityDAO;
import org.example.dao.SisterCityDAO;
import org.example.model.City;
import org.example.model.SisterCity;
import org.graph4j.Graph;
import org.graph4j.GraphBuilder;
import org.graph4j.connectivity.BiconnectivityAlgorithm;
import org.graph4j.util.Block;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BiconnectedComponentsFinder {
    private final Connection connection;
    private final CityDAO cityDAO;
    private final SisterCityDAO sisterCityDAO;

    public BiconnectedComponentsFinder(Connection connection) {
        this.connection = connection;
        this.cityDAO = new CityDAO(connection);
        this.sisterCityDAO = new SisterCityDAO(connection);
    }

    public List<List<City>> findBiconnectedComponents() throws SQLException {

        System.out.println("Fetching cities and relationships from database...");
        List<City> cities = cityDAO.findAll();

        String sql = "SELECT * FROM sister_cities";
        List<SisterCity> sisterCities = new ArrayList<>();
        try (var stmt = connection.createStatement();
             var rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                SisterCity sc = new SisterCity();
                sc.setId(rs.getInt("id"));
                sc.setCity1Id(rs.getInt("city1_id"));
                sc.setCity2Id(rs.getInt("city2_id"));
                sisterCities.add(sc);
            }
        }

        System.out.println("Building graph with " + cities.size() + " vertices and " +
                           sisterCities.size() + " edges");

        // cityID -> vertex
        Map<Integer, Integer> cityIdToVertexMap = new HashMap<>();
        Map<Integer, City> vertexToCityMap = new HashMap<>();

        for (int i = 0; i < cities.size(); i++) {
            City city = cities.get(i);
            cityIdToVertexMap.put(city.getId(), i);
            vertexToCityMap.put(i, city);
        }


        Graph graph = GraphBuilder.numVertices(cities.size())
                .estimatedAvgDegree(5)
                .buildGraph();


        for (SisterCity sisterCity : sisterCities) {
            Integer v1 = cityIdToVertexMap.get(sisterCity.getCity1Id());
            Integer v2 = cityIdToVertexMap.get(sisterCity.getCity2Id());

            if (v1 != null && v2 != null && !graph.containsEdge(v1, v2)) {
                graph.addEdge(v1, v2);
            }
        }


        System.out.println("Running biconnectivity algorithm...");
        BiconnectivityAlgorithm bccAlg = BiconnectivityAlgorithm.getInstance(graph);
        List<Block> biconnectedComponents = bccAlg.getBlocks();

        List<List<City>> result = new ArrayList<>();
        for (Block component : biconnectedComponents) {
            if (component.size() >= 3) {  // Optional filter
                List<City> cityComponent = new ArrayList<>();
                for (int vertex : component) {
                    cityComponent.add(vertexToCityMap.get(vertex));
                }
                result.add(cityComponent);
            }
        }

        System.out.println("Found " + result.size() + " biconnected components with 3+ cities");
        return result;
    }
}
