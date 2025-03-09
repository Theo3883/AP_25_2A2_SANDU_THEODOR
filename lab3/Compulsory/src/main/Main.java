package main;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Map;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        // Create aircraft
        Aircraft airliner1 = new Airliner("Boeing737", "737-800", 101, 180, 35);
        Aircraft airliner2 = new Airliner("AirbusA320", "A320", 102, 150, 36);
        Aircraft freighter = new Freighter("Boeing777", "777F", 301, 103000,30);
        Aircraft drone = new Drone("DJI", "Mavic3", 201, 40);

        // Create flights with different time slots using tail numbers
        Flight flight1 = new Flight("FL" + airliner1.getTailNumber(), LocalTime.of(10, 0), LocalTime.of(11, 0));
        Flight flight2 = new Flight("FL" + airliner2.getTailNumber(), LocalTime.of(10, 30), LocalTime.of(11, 30));
        //Flight flight3 = new Flight("FL" + freighter.getTailNumber(), LocalTime.of(11, 0), LocalTime.of(12, 0));
        //Flight flight4 = new Flight("FL" + drone.getTailNumber(), LocalTime.of(11, 30), LocalTime.of(12, 30));

        // Create airport with runways
        ArrayList<String> runways = new ArrayList<>(Arrays.asList("Runway1", "Runway2"));
        Airport airport = new Airport("International Airport", runways);

        // Create and solve scheduling problem
        SchedulingProblem problem = new SchedulingProblem(airport, Arrays.asList(flight1, flight2));

        try {
            Map<Flight, String> solution = problem.solve();

            // Print solution
            System.out.println("Flight Schedule:");
            solution.forEach((flight, runway) ->
                    System.out.printf("Flight %s: %s -> %s on %s%n",
                            flight.getFlightNumber(),
                            flight.getArrivalTime(),
                            flight.getDepartureTime(),
                            runway)
            );
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}