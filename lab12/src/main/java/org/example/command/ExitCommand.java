package org.example.command;

public class ExitCommand extends Command {
    
    public ExitCommand() {
        super("exit", "Exit the program");
    }
    
    @Override
    public boolean execute(String[] args) {
        System.out.println("Exiting program. Goodbye!");
        return false;
    }
}
