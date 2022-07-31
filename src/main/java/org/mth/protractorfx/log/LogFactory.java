package org.mth.protractorfx.log;

import java.util.logging.*;

public class LogFactory {

    static {
        LogManager.getLogManager().reset();
    }

    public static Logger configureLog(String logName) {
        Logger log = Logger.getLogger(logName);
        log.setLevel(Level.ALL);

        for (Handler h : log.getHandlers())
            log.removeHandler(h);

        ConsoleHandler handler = new ConsoleHandler();

        handler.setFormatter(new LogFormatter());
        handler.setLevel(Level.ALL);

        log.addHandler(handler);

        return log;
    }

    public static Logger configureLog(Class<?> clazz) {
        return configureLog(clazz.getName());
    }

    public static void main(String[] args) {
        Logger.getLogger("kkkk").severe("kkkkk");
        configureLog(LogFactory.class).finest("test");
    }
}
