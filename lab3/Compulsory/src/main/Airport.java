package main;

import java.util.ArrayList;
import java.util.List;

public class Airport {
    private final List<String> runways;
    private final String name;

    public Airport(String name,List<String> runways) {
        this.name = name;
        this.runways = new ArrayList<>(runways);
    }

    public void addRunway(String runway) {
        runways.add(runway);
    }

    public List<String> getRunways() {
        return new ArrayList<>(runways);
    }

    public String getName() {
        return name;
    }
}