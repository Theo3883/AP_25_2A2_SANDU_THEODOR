package org.example;

import org.example.command.*;
import org.example.loader.ClassLoader;
import org.example.test.TestRunner;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        ClassLoader classLoader = new ClassLoader();
        TestRunner testRunner = new TestRunner();
        Scanner scanner = new Scanner(System.in);
        
        List<Command> commands = new ArrayList<>();
        AnalyzeCommand analyzeCommand = new AnalyzeCommand(classLoader, testRunner);
        commands.add(analyzeCommand);
        
        ListCommand listCommand = new ListCommand(classLoader);
        commands.add(listCommand);
        
        ExitCommand exitCommand = new ExitCommand();
        commands.add(exitCommand);
        
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
                System.out.println("Unknown command: " + commandName);
                System.out.println("Type 'help' to see available commands");
            }
        }
        
        scanner.close();
    }
}