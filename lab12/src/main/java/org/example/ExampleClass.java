package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ExampleClass {
    public static void normalMethod() {
        System.out.println("This is a normal method");
    }

    @Test
    void testMethod1() {
        System.out.println("Running test method 1");
        assertTrue(true, "Test method 1 should pass");
    }

    @Test
    void testMethod2() {
        System.out.println("Running test method 2");
        assertTrue(true, "Test method 2 should pass");
    }

    public void nonStaticMethod() {
        System.out.println("This is a non-static method");
    }
} 