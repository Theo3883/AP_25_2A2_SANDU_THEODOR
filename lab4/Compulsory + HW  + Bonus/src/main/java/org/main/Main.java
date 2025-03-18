package org.main;

import com.github.javafaker.Faker;
import org.graph4j.Graph;
import org.graph4j.GraphBuilder;
import org.graph4j.*;
import org.graph4j.shortestpath.DijkstraShortestPathDefault;
import org.graph4j.util.Path;

import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        Faker faker = new Faker();
        Random random = new Random();
        ArrayList<Location> locations = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            String name = faker.address().city();
            Type type;
            int rand = random.nextInt(3);
            if (rand == 0) {
                type = Type.FRIENDLY;
            } else if (rand == 1) {
                type = Type.NEUTRAL;
            } else {
                type = Type.ENEMY;
            }
            locations.add(new Location(name, type));
        }
        TreeSet<Location> frindlyLocations = locations.stream()
                .filter(location -> location.getType() == Type.FRIENDLY)
                .collect(Collectors.toCollection(TreeSet::new));

        LinkedList<Location> enemyLocations = locations.stream()
                        .filter(location -> location.getType() == Type.ENEMY)
                        .sorted((location1,location2) ->{
                                    return location1.getName().compareTo(location2.getName());
                                })
                        .collect(Collectors.toCollection(LinkedList::new));

        ArrayList<Location> neutralLocations = locations.stream()
                        .filter(location -> location.getType() == Type.NEUTRAL)
                .collect(Collectors.toCollection(ArrayList::new));

        enemyLocations.forEach(location -> System.out.println(location));
        System.out.println("\n");
        frindlyLocations.forEach(location -> System.out.println(location));
        System.out.println("\n");
        neutralLocations.forEach(location -> System.out.println(location));

        ArrayList<Location> enemyLocationsArrayList = locations.stream()
                .filter(location -> location.getType() == Type.NEUTRAL)
                .collect(Collectors.toCollection(ArrayList::new));

        ArrayList<Location> frindlyLocationsArrayList = locations.stream()
                .filter(location -> location.getType() == Type.NEUTRAL)
                .collect(Collectors.toCollection(ArrayList::new));

        System.out.println("\n");
        System.out.println("Time for frindly locations: " + solveFastestRoute(frindlyLocationsArrayList));
        System.out.println("\n");

        System.out.println("Time for enemy locations: " +  solveFastestRoute(enemyLocationsArrayList) );

        System.out.println("\n");

        System.out.println("Time for neutral locations: "+solveFastestRoute(neutralLocations));
        System.out.println("\n");


        System.out.println("Safest route: " + solveSafestRoutes(locations) + "\n");

        System.out.println("Computing statistics...");

        // Generate random problems and run algorithms
        List<TestResult> testResults = generateAndRunTests(1000, 10);

        // Compute statistics
        computeStatistics(testResults);

    }

    public static String solveFastestRoute(ArrayList<Location> locations)
    {
        Random random = new Random();
        StringBuilder response = new StringBuilder();
        // Create a graph
        Graph graph = GraphBuilder.empty().buildGraph();

        // Add vertices
        for (Location location : locations) {
            graph.addVertex(locations.indexOf(location));
        }

        // Add edges with random weights and probabilities
        for (int i = 0; i < locations.size(); i++) {
            for (int j = i + 1; j < locations.size(); j++) {
                boolean canMoveDirectly = random.nextBoolean();
                double timeToTravel = 1 + (10 - 1) * random.nextDouble(); // Random time between 1 and 10
                double probabilityToReachSafely = random.nextDouble(); // Random probability between 0 and 1
                if (canMoveDirectly) {
                    graph.addEdge(locations.indexOf(locations.get(i)), locations.indexOf(locations.get(j)), timeToTravel);
                }
            }
        }

        // Choose a start location
        Location startLocation = locations.get(0);

        // Use Dijkstra's algorithm to find the shortest paths
        var dijkstra = new DijkstraShortestPathDefault(graph, locations.indexOf(startLocation));
        // Print the shortest paths to all other locations
        for (Location location : locations) {
            if (!location.equals(startLocation)) {
                response.append("\n"+"Shortest path from ").append(startLocation.getName()).append(" to ").append(location.getName()).append(": ").append(dijkstra.findPath(locations.indexOf(location))).append("\n");
                //System.out.println("Shortest path from " + startLocation.getName() + " to " + location.getName() + ": " + dijkstra.findPath(locations.indexOf(location)));
                response.append("Total travel time: ").append(dijkstra.getPathWeight(locations.indexOf(location)));
                //System.out.println("Total travel time: " + dijkstra.getPathWeight(locations.indexOf(location)));
            }
        }
        return response.toString();
    }

    public static String solveSafestRoutes(ArrayList<Location> locations) {
        StringBuilder response = new StringBuilder();
        Random random = new Random();
        Graph graph = GraphBuilder.empty().buildGraph();

        for (Location location : locations) {
            graph.addVertex(locations.indexOf(location));
        }

        for (int i = 0; i < locations.size(); i++) {
            for (int j = i + 1; j < locations.size(); j++) {
                boolean canMoveDirectly = random.nextBoolean();
                double timeToTravel = 1 + (10 - 1) * random.nextDouble();
                double probabilityToReachSafely = (locations.get(i).getType() == Type.ENEMY || locations.get(j).getType() == Type.ENEMY) ? 0.2 : 0.4;
                if (canMoveDirectly) {
                    graph.addEdge(locations.indexOf(locations.get(i)), locations.indexOf(locations.get(j)), probabilityToReachSafely);
                }
            }
        }

        Map<String, RouteInfo> routeInfoMap = new HashMap<>();

        for (Location startLocation : locations) {
            var dijkstra = new DijkstraShortestPathDefault(graph, locations.indexOf(startLocation));
            for (Location endLocation : locations) {
                if (!startLocation.equals(endLocation)) {
                    var path = dijkstra.findPath(locations.indexOf(endLocation));
                    path.iterator();
                    if (path != null) {
                        RouteInfo routeInfo = new RouteInfo();
                        StringBuilder routeString = new StringBuilder();
                        for (var vertex : path) {
                            Location location = locations.get(vertex);
                            routeInfo.incrementTypeCount(location.getType());
                        }
                        routeInfoMap.put(startLocation.getName() + " -> " + endLocation.getName(), routeInfo);
                    }
                }
            }
        }

        routeInfoMap.forEach((route, info) -> {
            response.append("\n" + "Route: ").append(route).append("\n");
            //System.out.println("Route: " + route);
            response.append("Friendly: ").append(info.getFriendlyCount()).append(", Neutral: ").append(info.getNeutralCount()).append(", Enemy: ").append(info.getEnemyCount());
            //System.out.println("Friendly: " + info.getFriendlyCount() + ", Neutral: " + info.getNeutralCount() + ", Enemy: " + info.getEnemyCount());
        });
        return response.toString();
    }

    public static List<TestResult> generateAndRunTests(int maxLocations, int numTests) {
        List<TestResult> testResults = new ArrayList<>();
        for (int i = 0; i < numTests; i++) {
            int numLocations = new Random().nextInt(maxLocations) + 1;
            List<Location> locations = generateRandomLocations(numLocations);
            String fastestRouteResult = solveFastestRoute(new ArrayList<>(locations));
            String safestRouteResult = solveSafestRoutes(new ArrayList<>(locations));
            testResults.add(new TestResult(numLocations, fastestRouteResult, safestRouteResult));
        }
        return testResults;
    }
    public static List<Location> generateRandomLocations(int numLocations) {
        Faker faker = new Faker();
        Random random = new Random();
        List<Location> locations = new ArrayList<>();
        for (int i = 0; i < numLocations; i++) {
            String name = faker.address().city();
            Type type = Type.values()[random.nextInt(Type.values().length)];
            locations.add(new Location(name, type));
        }
        return locations;
    }

    public static void computeStatistics(List<TestResult> testResults) {
        double averageLocations = testResults.stream()
                .mapToInt(TestResult::getNumLocations)
                .average()
                .orElse(0.0);

        int maxLocations = testResults.stream()
                .mapToInt(TestResult::getNumLocations)
                .max()
                .orElse(0);

        int minLocations = testResults.stream()
                .mapToInt(TestResult::getNumLocations)
                .min()
                .orElse(0);

        System.out.println("Average number of locations: " + averageLocations);
        System.out.println("Maximum number of locations: " + maxLocations);
        System.out.println("Minimum number of locations: " + minLocations);
    }

}