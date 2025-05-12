package org.example.commands;

import org.example.server.ClientThread;
import org.example.game.GameManager;
import org.example.server.GameServer;

import java.util.Map;

public class HelpCommand extends Command {
    private final Map<String, Command> commandMap;

    public HelpCommand(ClientThread clientThread, GameServer server, GameManager gameManager, Map<String, Command> commandMap) {
        super(clientThread, server, gameManager);
        this.commandMap = commandMap;
    }

    @Override
    public String execute(String[] args) {
        StringBuilder help = new StringBuilder("Available commands:\n");
        commandMap.forEach((name, command) -> 
            help.append(command.getUsage()).append(" - ").append(command.getDescription()).append("\n")
        );
        return help.toString();
    }

    @Override
    public String getDescription() {
        return "Show this help";
    }

    @Override
    public String getUsage() {
        return "help";
    }
}
