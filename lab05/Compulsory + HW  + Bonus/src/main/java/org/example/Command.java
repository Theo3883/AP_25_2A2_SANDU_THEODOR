package org.example;

import java.io.IOException;
import java.util.List;

import exceptions.CommandNotFoundException;
import exceptions.InvalidCommandArgumentsException;
import exceptions.InvalidRepositoryException;
import exceptions.ShellExecutionException;

interface Command {
    void execute(List<String> args) throws CommandNotFoundException,
            InvalidCommandArgumentsException,
            InvalidRepositoryException,
            IOException,
            ShellExecutionException;

    String getName();

    String getDescription();
}