package org.example.command;

import org.example.loader.ClassLoader;
import org.example.test.TestRunner;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class AnalyzeCommand extends Command {
    
    private final ClassLoader classLoader;
    private final TestRunner testRunner;
    
    public AnalyzeCommand(ClassLoader classLoader, TestRunner testRunner) {
        super("analyze", "Analyze a class or classes in a directory");
        this.classLoader = classLoader;
        this.testRunner = testRunner;
    }
    
    @Override
    public boolean execute(String[] args) {
        if (args.length < 1) {
            System.out.println("Error: Please provide a path or fully qualified class name");
            return true;
        }
        
        String path = args[0];
        try {
            classLoader.clear();
            
            if (classLoader.isLoadableClassName(path)) {
                Class<?> clazz = classLoader.loadClassByName(path);
                if (clazz != null) {
                    analyzeClass(clazz);
                } else {
                    System.out.println("Error: Class not found: " + path);
                }
            } else if (path.contains("/") || path.contains("\\") || path.endsWith(".class") || path.endsWith(".jar")) {
                classLoader.addClassPath(path);
                for (Class<?> loadedClass : classLoader.loadClasses()) {
                    analyzeClass(loadedClass);
                }
            } else {
                System.out.println("Error: Class not found or invalid path: " + path);
                System.out.println("Please provide a fully qualified class name or a valid file/directory path");
            }
            
            testRunner.printStatistics();
            return true;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            return true;
        }
    }
    
    private void analyzeClass(Class<?> clazz) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Class: " + clazz.getName());
        System.out.println("-".repeat(80));

        System.out.println("Modifiers: " + Modifier.toString(clazz.getModifiers()));

        if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)) {
            System.out.println("Superclass: " + clazz.getSuperclass().getName());
        }

        if (clazz.getInterfaces().length > 0) {
            System.out.println("Interfaces: " + Arrays.toString(
                Arrays.stream(clazz.getInterfaces())
                    .map(Class::getName)
                    .toArray()
            ));
        }

        System.out.println("\nFields:");
        for (Field field : clazz.getDeclaredFields()) {
            System.out.println("- " + Modifier.toString(field.getModifiers()) + " " + 
                             field.getType().getName() + " " + field.getName());
        }

        System.out.println("\nConstructors:");
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            System.out.println("- " + Modifier.toString(constructor.getModifiers()) + " " + 
                             constructor.getName().substring(constructor.getName().lastIndexOf('.') + 1) + "(" + 
                             formatParameters(constructor.getParameterTypes()) + ")");
        }

        System.out.println("\nMethods:");
        for (Method method : clazz.getDeclaredMethods()) {
            System.out.println("- " + Modifier.toString(method.getModifiers()) + " " + 
                             method.getReturnType().getName() + " " + 
                             method.getName() + "(" + 
                             formatParameters(method.getParameterTypes()) + ")");
        }
        
        // Execute @Test
        boolean hasTestAnnotation = Arrays.stream(clazz.getDeclaredMethods())
            .anyMatch(method -> method.isAnnotationPresent(Test.class));
        
        if (hasTestAnnotation) {
            System.out.println("\nRunning tests for: " + clazz.getName());
            testRunner.runTests(clazz);
        }
    }
    
    private String formatParameters(Class<?>[] parameterTypes) {
        return Arrays.stream(parameterTypes)
            .map(Class::getName)
            .reduce((a, b) -> a + ", " + b)
            .orElse("");
    }
}
