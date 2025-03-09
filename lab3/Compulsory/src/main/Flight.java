package main;

import java.time.LocalTime;

public class Flight {
    private final LocalTime arrivalTime;
    private final LocalTime departureTime;
    private final String flightNumber;

    public Flight(String flightNumber, LocalTime arrivalTime, LocalTime departureTime) {
        this.flightNumber = flightNumber;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public String getFlightNumber() {
        return flightNumber;
    }
}