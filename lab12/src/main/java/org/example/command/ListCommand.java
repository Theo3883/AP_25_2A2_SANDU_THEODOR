package org.example.command;

import org.apache.logging.log4j.Logger;
import org.example.loader.ClassLoader;
import org.example.util.LoggerUtil;

import java.util.List;

public class ListCommand extends Command {
    private static final Logger logger = LoggerUtil.getInstance().createLogger(ListCommand.class);
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
                logger.info("No classes found in the current project.");
                return true;
            }
            
            logger.info("Available classes in the current project:");
            for (String className : availableClasses) {
                logger.info("  - " + className);
            }
            
            logger.info("\nUse 'analyze <class name>' to analyze a specific class");
            return true;
        } catch (Exception e) {
            logger.error("Error listing classes: " + e.getMessage());
            return true;
        }
    }
}
