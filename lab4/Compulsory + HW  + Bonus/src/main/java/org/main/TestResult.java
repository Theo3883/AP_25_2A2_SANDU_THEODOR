package org.main;

public class TestResult {
    private final int numLocations;
    private final String fastestRouteResult;
    private final String safestRouteResult;

    public TestResult(int numLocations, String fastestRouteResult, String safestRouteResult) {
        this.numLocations = numLocations;
        this.fastestRouteResult = fastestRouteResult;
        this.safestRouteResult = safestRouteResult;
    }

    public int getNumLocations() {
        return numLocations;
    }

    public String getFastestRouteResult() {
        return fastestRouteResult;
    }

    public String getSafestRouteResult() {
        return safestRouteResult;
    }
}