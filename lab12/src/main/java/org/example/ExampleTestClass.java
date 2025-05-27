package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class ExampleTestClass {
    private int value = 0;

    @BeforeEach
    void setUp() {
        value = 0;
    }

    @Test
    void testAddition() {
        value += 5;
        assertEquals(5, value, "Value should be 5 after addition");
    }

    @Test
    void testSubtraction() {
        value -= 3;
        assertEquals(-3, value, "Value should be -3 after subtraction");
    }

    @Test
    void testMultiplication() {
        value = 4;
        value *= 2;
        assertEquals(8, value, "Value should be 8 after multiplication");
    }

    @Test
    void testDivision() {
        value = 10;
        value /= 2;
        assertEquals(5, value, "Value should be 5 after division");
    }

    @Test
    void testFailingAssertion() {
        value = 5;
        assertNotEquals(5, value, "This test should fail");
    }
} 