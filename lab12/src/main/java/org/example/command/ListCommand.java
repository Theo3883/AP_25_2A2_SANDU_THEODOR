package org.example.command;

import org.example.loader.ClassLoader;

import java.util.List;

public class ListCommand extends Command {
    
    private final ClassLoader classLoader;
    
    public ListCommand(ClassLoader classLoader) {
        super("list", "List all available classes in the current project");
        this.classLoader = classLoader;
    }
    
    @Override
    public boolean execute(String[] args) {
        try {
            List<String> availableClasses = classLoader.listAvailableClasses();
            
            if (availableClasses.isEmpty()) {
                System.out.println("No classes found in the current project.");
                return true;
            }
            
            System.out.println("Available classes in the current project:");
            for (String className : availableClasses) {
                System.out.println("  - " + className);
            }
            
            System.out.println("\nUse 'analyze <class name>' to analyze a specific class");
            return true;
        } catch (Exception e) {
            System.out.println("Error listing classes: " + e.getMessage());
            return true;
        }
    }
}
