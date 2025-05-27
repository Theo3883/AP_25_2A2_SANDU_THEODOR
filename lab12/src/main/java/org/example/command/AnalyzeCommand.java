package org.example.command;

import org.apache.logging.log4j.Logger;
import org.example.loader.ClassLoader;
import org.example.test.TestRunner;
import org.example.util.LoggerUtil;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class AnalyzeCommand extends Command {
    private static final Logger logger = LoggerUtil.getInstance().createLogger(AnalyzeCommand.class);
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
            logger.error("Error: Please provide a path or fully qualified class name");
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
                    logger.error("Error: Class not found: " + path);
                }
            } else if (path.contains("/") || path.contains("\\") || path.endsWith(".class") || path.endsWith(".jar")) {
                classLoader.addClassPath(path);
                for (Class<?> loadedClass : classLoader.loadClasses()) {
                    analyzeClass(loadedClass);
                }
            } else {
                logger.error("Error: Class not found or invalid path: " + path);
                logger.info("Please provide a fully qualified class name or a valid file/directory path");
            }
            
            testRunner.printStatistics();
            return true;
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
            e.printStackTrace();
            return true;
        }
    }
    
    private void analyzeClass(Class<?> clazz) {
        logger.info("\n" + "=".repeat(80));
        logger.info("Class: " + clazz.getName());
        logger.info("-".repeat(80));

        logger.info("Modifiers: " + Modifier.toString(clazz.getModifiers()));

        if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)) {
            logger.info("Superclass: " + clazz.getSuperclass().getName());
        }

        if (clazz.getInterfaces().length > 0) {
            logger.info("Interfaces: " + Arrays.toString(
                Arrays.stream(clazz.getInterfaces())
                    .map(Class::getName)
                    .toArray()
            ));
        }

        logger.info("\nFields:");
        for (Field field : clazz.getDeclaredFields()) {
            logger.info("- " + Modifier.toString(field.getModifiers()) + " " + 
                     field.getType().getName() + " " + field.getName());
        }

        logger.info("\nConstructors:");
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            logger.info("- " + Modifier.toString(constructor.getModifiers()) + " " + 
                     constructor.getName().substring(constructor.getName().lastIndexOf('.') + 1) + "(" + 
                     formatParameters(constructor.getParameterTypes()) + ")");
        }

        logger.info("\nMethods:");
        for (Method method : clazz.getDeclaredMethods()) {
            logger.info("- " + Modifier.toString(method.getModifiers()) + " " + 
                     method.getReturnType().getName() + " " + 
                     method.getName() + "(" + 
                     formatParameters(method.getParameterTypes()) + ")");
        }
        
        // Execute @Test
        boolean hasTestAnnotation = Arrays.stream(clazz.getDeclaredMethods())
            .anyMatch(method -> method.isAnnotationPresent(Test.class));
        
        if (hasTestAnnotation) {
            logger.info("\nRunning tests for: " + clazz.getName());
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
