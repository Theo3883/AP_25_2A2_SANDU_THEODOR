package org.example;

import org.apache.logging.log4j.Logger;
import org.example.command.*;
import org.example.loader.ClassLoader;
import org.example.test.TestRunner;
import org.example.util.LoggerUtil;

import java.util.*;

public class Main {
    private static final Logger logger = LoggerUtil.getInstance().createLogger(Main.class);
    
    public static void main(String[] args) {
        ClassLoader classLoader = new ClassLoader();
        TestRunner testRunner = new TestRunner();
        Scanner scanner = new Scanner(System.in);
        
        List<Command> commands = new ArrayList<>();
        commands.add(new AnalyzeCommand(classLoader, testRunner));
        commands.add(new ListCommand(classLoader));
        commands.add(new BytecodeCommand(classLoader));
        commands.add(new ExitCommand());
        
        HelpCommand helpCommand = new HelpCommand(commands);
        commands.add(helpCommand);
        
        Map<String, Command> commandMap = new HashMap<>();
        for (Command command : commands) {
            commandMap.put(command.getName(), command);
        }
        
        helpCommand.execute(new String[0]);
        
        boolean continueRunning = true;
        while (continueRunning) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            String[] parts = input.split("\\s+", 2);
            String commandName = parts[0].toLowerCase();
            
            Command command = commandMap.get(commandName);
            if (command != null) {
                String[] commandArgs = parts.length > 1 ? parts[1].trim().split("\\s+") : new String[0];
                continueRunning = command.execute(commandArgs);
            } else {
                logger.error("Unknown command: " + commandName);
                logger.info("Type 'help' to see available commands");
            }
        }
        
        scanner.close();
    }
}