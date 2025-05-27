package org.example;

import org.apache.logging.log4j.Logger;
import org.example.util.LoggerUtil;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.junit.platform.engine.discovery.DiscoverySelectors;

public class TestRunner {
    private static final Logger logger = LoggerUtil.getInstance().createLogger(TestRunner.class);
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

            logger.info("\nTest Results:");
            logger.info("  Started: " + summary.getTestsStartedCount());
            logger.info("  Succeeded: " + summary.getTestsSucceededCount());
            logger.info("  Failed: " + summary.getTestsFailedCount());

            if (summary.getTestsFailedCount() > 0) {
                logger.info("\nFailed Tests:");
                summary.getFailures().forEach(failure -> {
                    logger.info("  - " + failure.getTestIdentifier().getDisplayName());
                    logger.info("    Reason: " + failure.getException().getMessage());
                });
            }
            
        } catch (Exception e) {
            logger.error("Error running tests for " + testClass.getName() + ": " + e.getMessage());
        }
    }

    public void printStatistics() {
        logger.info("\n" + "=".repeat(80));
        logger.info("Test Execution Summary");
        logger.info("-".repeat(80));
        logger.info("Total Tests: " + totalTests);
        logger.info("Passed: " + passedTests);
        logger.info("Failed: " + failedTests);
        logger.info("=".repeat(80));
    }
}