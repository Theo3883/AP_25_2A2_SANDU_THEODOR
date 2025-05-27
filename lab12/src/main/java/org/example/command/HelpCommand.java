package org.example.command;

import org.apache.logging.log4j.Logger;
import org.example.util.LoggerUtil;

import java.util.List;

public class HelpCommand extends Command {
    private static final Logger logger = LoggerUtil.getInstance().createLogger(HelpCommand.class);
    private final List<Command> commands;
    
    public HelpCommand(List<Command> commands) {
        super("help", "Display available commands");
        this.commands = commands;
    }
    
    @Override
    public boolean execute(String[] args) {
        logger.info("Available commands:");
        
        for (Command command : commands) {
            logger.info(String.format("  %-10s - %s", command.getName(), command.getDescription()));
        }
        
        return true;
    }
}
