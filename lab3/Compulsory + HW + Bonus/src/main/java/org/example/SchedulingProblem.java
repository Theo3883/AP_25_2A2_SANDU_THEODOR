package org.example;

import java.time.LocalTime;
import java.util.*;

public class SchedulingProblem {
    private final Airport airport;
    private final List<Flight> flights;
    private final Map<Flight, String> flightToRunway;
    private boolean solved = false;
    private int additionalRunwaysNeeded = 0;

    public SchedulingProblem(Airport airport, List<Flight> flights) {
        this.airport = airport;
        this.flights = new ArrayList<>(flights);
        this.flightToRunway = new HashMap<>();
    }

    public void solve() {
        if (flights.isEmpty()) {
            solved = true;
            return;
        }

        // Sort flights by arrival time
        flights.sort(Comparator.comparing(Flight::getArrivalTime));

        // Map to track when each runway becomes available (runway -> time)
        Map<String, LocalTime> runwayAvailability = new HashMap<>();
        for (String runway : airport.getRunways()) {
            runwayAvailability.put(runway, LocalTime.MIN);
        }

        // Map to count flights per runway
        Map<String, Integer> runwayUsageCount = new HashMap<>();
        for (String runway : airport.getRunways()) {
            runwayUsageCount.put(runway, 0);
        }

        // Maximum delay
        final int maxDelay = 30;

        // Try to assign each flight
        for (Flight flight : flights) {

            // Find available runway with minimum usage
            String selectedRunway = null;
            int minUsage = Integer.MAX_VALUE;
            LocalTime earliestAvailableTime = LocalTime.MAX;

            for (String runway : airport.getRunways()) {
                LocalTime availableTime = runwayAvailability.get(runway);
                int usageCount = runwayUsageCount.get(runway);

                // If runway is available at or before flight arrival time
                if (!availableTime.isAfter(flight.getArrivalTime()) && usageCount < minUsage) {
                    selectedRunway = runway;
                    minUsage = usageCount;
                    earliestAvailableTime = availableTime;
                }

                // Track the earliest available time across all runways
                else if (availableTime.isBefore(earliestAvailableTime)) {
                    earliestAvailableTime = availableTime;
                }
            }

            // If no runway is available at flight arrival time
            if (selectedRunway == null) {

                // Try to find any runway with minimum usage
                for (String runway : airport.getRunways()) {
                    int usageCount = runwayUsageCount.get(runway);
                    LocalTime availableTime = runwayAvailability.get(runway);

                    if ((selectedRunway == null || usageCount < minUsage) &&
                            !availableTime.isAfter(flight.getArrivalTime().plusMinutes(maxDelay))) {
                        selectedRunway = runway;
                        minUsage = usageCount;
                    }
                }

                // If still no runway, we need additional runways
                if (selectedRunway == null) {
                    additionalRunwaysNeeded++;
                    // Create a virtual runway
                    String virtualRunway = "VirtualRunway" + additionalRunwaysNeeded;
                    selectedRunway = virtualRunway;
                    runwayAvailability.put(virtualRunway, LocalTime.MIN);
                    runwayUsageCount.put(virtualRunway, 0);
                }
            }

            // Assign flight to selected runway
            flightToRunway.put(flight, selectedRunway);

            // Update runway availability and usage count
            runwayAvailability.put(selectedRunway, flight.getDepartureTime());
            runwayUsageCount.put(selectedRunway, runwayUsageCount.get(selectedRunway) + 1);
        }

        // Check distribution
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int count : runwayUsageCount.values()) {
            min = Math.min(min, count);
            max = Math.max(max, count);
        }

        if (max - min > 1) {
            // Try to delay some flights to achieve better distribution
            rebalanceSchedule(runwayUsageCount);
        }

        solved = true;
    }

    private void rebalanceSchedule(Map<String, Integer> runwayUsageCount) {
        // Find overused and underused runways
        List<String> overusedRunways = new ArrayList<>();
        List<String> underusedRunways = new ArrayList<>();

        int avg = (int) Math.ceil((double) flights.size() / airport.getRunways().size());

        for (Map.Entry<String, Integer> entry : runwayUsageCount.entrySet()) {
            if (entry.getValue() > avg) {
                overusedRunways.add(entry.getKey());
            } else if (entry.getValue() < avg - 1) {
                underusedRunways.add(entry.getKey());
            }
        }

        if (overusedRunways.isEmpty() || underusedRunways.isEmpty()) {
            return;
        }

        // For each overused runway, try to move flights to underused runways
        for (String overusedRunway : overusedRunways) {
            List<Flight> flightsOnRunway = new ArrayList<>();
            for (Map.Entry<Flight, String> entry : flightToRunway.entrySet()) {
                if (entry.getValue().equals(overusedRunway)) {
                    flightsOnRunway.add(entry.getKey());
                }
            }

            // Sort flights by departure time (latest first for easier reallocation)
            flightsOnRunway.sort(Comparator.comparing(Flight::getDepartureTime).reversed());

            for (Flight flight : flightsOnRunway) {
                for (String underusedRunway : underusedRunways) {
                    if (runwayUsageCount.get(overusedRunway) > runwayUsageCount.get(underusedRunway) + 1) {
                        // Move flight to underused runway
                        flightToRunway.put(flight, underusedRunway);
                        runwayUsageCount.put(overusedRunway, runwayUsageCount.get(overusedRunway) - 1);
                        runwayUsageCount.put(underusedRunway, runwayUsageCount.get(underusedRunway) + 1);
                    }
                }
            }
        }
    }

    public Map<Flight, String> getSolution() {
        if (!solved) {
            throw new IllegalStateException("Problem not solved yet");
        }
        return Collections.unmodifiableMap(flightToRunway);
    }

    public int getAdditionalRunwaysNeeded() {
        if (!solved) {
            throw new IllegalStateException("Problem not solved yet");
        }
        return additionalRunwaysNeeded;
    }
}