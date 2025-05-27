package org.example;

import java.util.Scanner;
import java.util.Arrays;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;

public class Main {
    public static void main(String[] args) {
        ClassLoader classLoader = new ClassLoader();
        TestRunner testRunner = new TestRunner();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Available commands:");
        System.out.println("  analyze <path> - Analyze all classes in a directory or file");
        System.out.println("  exit - Exit the program");

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                break;
            } else if (input.startsWith("analyze ")) {
                String path = input.substring(8).trim();
                if (path.isEmpty()) {
                    System.out.println("Error: Please provide a path");
                    continue;
                }

                try {
                    classLoader = new ClassLoader();
                    classLoader.addClassPath(path);
                    
                    for (Class<?> clazz : classLoader.loadClasses()) {
                        System.out.println("\n" + "=".repeat(80));
                        System.out.println("Class: " + clazz.getName());
                        System.out.println("-".repeat(80));

                        System.out.println("Modifiers: " + Modifier.toString(clazz.getModifiers()));

                        if (clazz.getSuperclass() != null) {
                            System.out.println("Superclass: " + clazz.getSuperclass().getName());
                        }

                        if (clazz.getInterfaces().length > 0) {
                            System.out.println("Interfaces: " + Arrays.toString(clazz.getInterfaces()));
                        }

                        System.out.println("\nFields:");
                        for (Field field : clazz.getDeclaredFields()) {
                            System.out.println("- " + Modifier.toString(field.getModifiers()) + " " + 
                                             field.getType().getName() + " " + field.getName());
                        }

                        System.out.println("\nConstructors:");
                        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                            System.out.println("- " + Modifier.toString(constructor.getModifiers()) + " " + 
                                             constructor.getName() + "(" + 
                                             Arrays.toString(constructor.getParameterTypes()) + ")");
                        }

                        System.out.println("\nMethods:");
                        for (Method method : clazz.getDeclaredMethods()) {
                            System.out.println("- " + Modifier.toString(method.getModifiers()) + " " + 
                                             method.getReturnType().getName() + " " + 
                                             method.getName() + "(" + 
                                             Arrays.toString(method.getParameterTypes()) + ")");
                        }
                        
                        // Execute @Test
                        boolean hasTestAnnotation = Arrays.stream(clazz.getDeclaredMethods())
                            .anyMatch(method -> method.isAnnotationPresent(org.junit.jupiter.api.Test.class));
                        
                        if (hasTestAnnotation) {
                            System.out.println("\nRunning tests for: " + clazz.getName());
                            testRunner.runTests(clazz);
                        }
                    }
                    
                    testRunner.printStatistics();
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            } else {
                System.out.println("Unknown command");
            }
        }
        
        scanner.close();
    }
}