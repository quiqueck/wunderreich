package de.ambertation.wunderreich.utils;

import de.ambertation.wunderreich.Wunderreich;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

public class Logger {
    private final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(Wunderreich.MOD_ID);

    public Logger() {

    }


    public void info(String message) {
        LOGGER.log(Level.INFO, message);
    }

    public void info(String message, Object... params) {
        LOGGER.log(Level.INFO, message, params);
    }

    public void warn(String message, Object... params) {
        LOGGER.log(Level.WARN, message, params);
    }

    public void warn(String message, Object obj, Exception ex) {
        LOGGER.log(Level.WARN, message, obj, ex);
    }

    public void error(String message) {
        LOGGER.log(Level.ERROR, message);
    }

    public void error(String message, Object obj, Exception ex) {
        LOGGER.error(message, obj, ex);
    }

    public void error(String message, Exception ex) {
        LOGGER.error(message, ex);
    }
}
