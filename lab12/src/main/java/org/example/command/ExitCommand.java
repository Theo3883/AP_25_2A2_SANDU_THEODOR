package org.example.command;

import org.apache.logging.log4j.Logger;
import org.example.util.LoggerUtil;

public class ExitCommand extends Command {
    private static final Logger logger = LoggerUtil.getInstance().createLogger(ExitCommand.class);
    
    public ExitCommand() {
        super("exit", "Exit the program");
    }
    
    @Override
    public boolean execute(String[] args) {
        logger.info("Exiting program. Goodbye!");
        return false;
    }
}
