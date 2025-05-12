package org.main;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TestResult {
    private final int numLocations;
    private final String fastestRouteResult;
    private final String safestRouteResult;

}