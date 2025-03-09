package main;

import java.util.*;

public class SchedulingProblem {
    private final Airport airport;
    private final List<Flight> flights;
    private final Map<Flight, String> flightToRunway;

    public SchedulingProblem(Airport airport, List<Flight> flights) {
        this.airport = airport;
        this.flights = new ArrayList<>(flights);
        this.flightToRunway = new HashMap<>();
    }

    public Map<Flight, String> solve() {
        // Sort flights by arrival time
        flights.sort((f1, f2) -> f1.getArrivalTime().compareTo(f2.getArrivalTime()));

        // Keep track of runway availability times
        Map<String, Flight> runwayLastFlight = new HashMap<>();
        List<String> runways = airport.getRunways();

        // Assign each flight to the first available runway
        for (Flight flight : flights) {
            boolean assigned = false;

            for (String runway : runways) {
                Flight lastFlight = runwayLastFlight.get(runway);

                if (lastFlight == null ||
                        lastFlight.getDepartureTime().isBefore(flight.getArrivalTime())) {
                    flightToRunway.put(flight, runway);
                    runwayLastFlight.put(runway, flight);
                    assigned = true;
                    break;
                }
            }

            if (!assigned) {
                throw new IllegalStateException("Unable to assign flight " +
                        flight.getFlightNumber() + " to any runway");
            }
        }

        return Collections.unmodifiableMap(flightToRunway);
    }

    public Map<Flight, String> getSolution() {
        return Collections.unmodifiableMap(flightToRunway);
    }
}