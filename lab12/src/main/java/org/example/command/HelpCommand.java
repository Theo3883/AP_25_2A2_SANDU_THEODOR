package org.example.command;

import java.util.List;

public class HelpCommand extends Command {
    
    private final List<Command> commands;
    
    public HelpCommand(List<Command> commands) {
        super("help", "Display available commands");
        this.commands = commands;
    }
    
    @Override
    public boolean execute(String[] args) {
        System.out.println("Available commands:");
        
        for (Command command : commands) {
            System.out.printf("  %-10s - %s%n", command.getName(), command.getDescription());
        }
        
        return true;
    }
}
