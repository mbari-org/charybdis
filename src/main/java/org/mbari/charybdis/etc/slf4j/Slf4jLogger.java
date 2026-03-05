package org.mbari.charybdis.etc.slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class Slf4jLogger implements System.Logger {

    private final String name;
    private final Logger logger;

    public Slf4jLogger(String name) {
        this.name = name;
        logger = LoggerFactory.getLogger(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isLoggable(Level level) {
        return switch (level) {
            case Level.OFF -> false;
            case Level.TRACE -> logger.isTraceEnabled();
            case Level.DEBUG -> logger.isDebugEnabled();
            case Level.INFO -> logger.isInfoEnabled();
            case Level.WARNING -> logger.isWarnEnabled();
            case Level.ERROR -> logger.isErrorEnabled();
            case Level.ALL -> true;
        };
    }

    @Override
    public void log(Level level, ResourceBundle bundle, String msg, Throwable thrown) {
        if (isLoggable(level)) {
            switch (level) {
                case Level.TRACE -> logger.trace(msg, thrown);
                case Level.DEBUG -> logger.debug(msg, thrown);
                case Level.INFO -> logger.info(msg, thrown);
                case Level.WARNING -> logger.warn(msg, thrown);
                case Level.ERROR -> logger.error(msg, thrown);
            }
        }
    }

    @Override
    public void log(Level level, ResourceBundle bundle, String format, Object... params) {
        if (isLoggable(level)) {
            var msg = params != null ? MessageFormat.format(format, params) : format;
            switch (level) {
                case Level.TRACE -> logger.trace(msg);
                case Level.DEBUG -> logger.debug(msg);
                case Level.INFO -> logger.info(msg);
                case Level.WARNING -> logger.warn(msg);
                case Level.ERROR -> logger.error(msg);
            }
        }
    }


}
