package org.example;

import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.junit.platform.engine.discovery.DiscoverySelectors;

public class TestRunner {
    private int totalTests = 0;
    private int passedTests = 0;
    private int failedTests = 0;

    public void runTests(Class<?> testClass) {
        try {
            LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder
                .request()
                .selectors(DiscoverySelectors.selectClass(testClass))
                .build();

            Launcher launcher = LauncherFactory.create();

            SummaryGeneratingListener listener = new SummaryGeneratingListener();
            launcher.registerTestExecutionListeners(listener);

            launcher.execute(request);

            TestExecutionSummary summary = listener.getSummary();

            totalTests += summary.getTestsStartedCount();
            passedTests += summary.getTestsSucceededCount();
            failedTests += summary.getTestsFailedCount();

            System.out.println("\nTest Results:");
            System.out.println("  Started: " + summary.getTestsStartedCount());
            System.out.println("  Succeeded: " + summary.getTestsSucceededCount());
            System.out.println("  Failed: " + summary.getTestsFailedCount());

            if (summary.getTestsFailedCount() > 0) {
                System.out.println("\nFailed Tests:");
                summary.getFailures().forEach(failure -> {
                    System.out.println("  - " + failure.getTestIdentifier().getDisplayName());
                    System.out.println("    Reason: " + failure.getException().getMessage());
                });
            }
            
        } catch (Exception e) {
            System.err.println("Error running tests for " + testClass.getName() + ": " + e.getMessage());
        }
    }

    public void printStatistics() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Test Execution Summary");
        System.out.println("-".repeat(80));
        System.out.println("Total Tests: " + totalTests);
        System.out.println("Passed: " + passedTests);
        System.out.println("Failed: " + failedTests);
        System.out.println("=".repeat(80));
    }
} 