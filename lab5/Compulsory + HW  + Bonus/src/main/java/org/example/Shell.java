// Shell.java
package org.example;

import exceptions.CommandNotFoundException;
import exceptions.InvalidCommandArgumentsException;
import exceptions.InvalidRepositoryException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Shell {
    private static Shell instance;
    private final Map<String, Command> commands = new HashMap<>();
    private boolean running = true;
    private Path currentDirectory = Paths.get(System.getProperty("user.dir"));
    private Repository repo;
    public Shell() {

        repo = new Repository();

        // Register commands
        registerCommand(new ChangeDirectoryCommand());
        registerCommand(new ExitCommand(this));
        registerCommand(new ListCommand());
        registerCommand(new AddImageCommand());
        registerCommand(new RemoveImageCommand());
        registerCommand(new UpdateImageCommand());
        registerCommand(new ListImagesCommand());
        registerCommand(new HelpCommand());
        registerCommand(new ReportCommand());
        registerCommand(new SaveCommand());
        registerCommand(new LoadCommand());
        registerCommand(new AddAllCommand());
        registerCommand(new FindGroupsCommand());

    }

    public static Shell getInstance() {
        if (instance == null) {
            instance = new Shell();
        }
        return instance;
    }

    public Repository getRepository() {
        return repo;
    }

    private void registerCommand(Command command) {
        commands.put(command.getName().toLowerCase(), command);
    }

    public void stop() {
        running = false;
    }

    public Map<String, Command> getCommands() {
        return commands;
    }

    public Path getCurrentDirectory() {
        return currentDirectory;
    }

    public void setCurrentDirectory(Path path) {
        this.currentDirectory = path;
    }

    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Welcome to Java NIO Shell. Type 'help' for available commands.");

        while (running) {
            try {
                System.out.print(currentDirectory + "> ");
                String input = reader.readLine();

                if (input == null || input.trim().isEmpty()) {
                    continue;
                }

                String[] parts = input.trim().split("\\s+", 2);
                String commandName = parts[0].toLowerCase();

                try {
                    if (!commands.containsKey(commandName)) {
                        throw new CommandNotFoundException(commandName);
                    }

                    List<String> args = parts.length > 1 ?
                            Arrays.asList(parts[1].split("\\s+")) :
                            new ArrayList<>();

                    commands.get(commandName).execute(args);
                } catch (CommandNotFoundException e) {
                    System.err.println(e.getMessage());
                    System.err.println("Type 'help' for available commands.");
                } catch (InvalidCommandArgumentsException e) {
                    System.err.println(e.getMessage());
                    Command cmd = commands.get(commandName);
                    if (cmd != null) {
                        System.err.println("Usage: " + cmd.getDescription());
                    }
                } catch (InvalidRepositoryException e) {
                    System.err.println(e.getMessage());
                    if (e.getCause() != null) {
                        System.err.println("Cause: " + e.getCause().getMessage());
                    }
                } catch (IOException e) {
                    System.err.println("I/O Error: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Error executing command: " + e.getMessage());
                }
            } catch (IOException e) {
                System.err.println("Error reading input: " + e.getMessage());
            }
        }
    }

}