package org.example.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoggerUtil {
    private static LoggerUtil instance;

    private LoggerUtil() {}

    public static synchronized LoggerUtil getInstance() {
        if (instance == null) {
            instance = new LoggerUtil();
        }
        return instance;
    }

    public Logger createLogger(Class<?> clazz) {
        return LogManager.getLogger(clazz);
    }
}
