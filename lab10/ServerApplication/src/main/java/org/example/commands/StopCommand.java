package org.example.commands;

import org.example.server.ClientThread;
import org.example.game.GameManager;
import org.example.server.GameServer;

public class StopCommand extends Command {

    public StopCommand(ClientThread clientThread, GameServer server, GameManager gameManager) {
        super(clientThread, server, gameManager);
    }

    @Override
    public String execute(String[] args) {
        server.stop();
        return "Server stopped";
    }

    @Override
    public String getDescription() {
        return "Stop the server";
    }

    @Override
    public String getUsage() {
        return "stop";
    }
}
