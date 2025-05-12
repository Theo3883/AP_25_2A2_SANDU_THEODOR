package exceptions;

public class CommandNotFoundException extends Exception {
    public CommandNotFoundException(String commandName) {
        super("Command not found: " + commandName);
    }
}