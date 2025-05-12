package org.example.commands;

import lombok.RequiredArgsConstructor;
import org.example.server.ClientThread;
import org.example.game.GameManager;
import org.example.server.GameServer;


@RequiredArgsConstructor
public abstract class Command {
    protected final ClientThread clientThread;
    protected final GameServer server;
    protected final GameManager gameManager;


    public abstract String execute(String[] args);

    public abstract String getDescription();

    public abstract String getUsage();
}
