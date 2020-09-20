package net.dirtcraft.dirtlauncher.logging;

import net.dirtcraft.dirtlauncher.configuration.Constants;

public interface Logger {
    Logger INSTANCE = getInstance();
    void info(String s);
    void info(Object o);
    void debug(String s);
    void debug(Object o);
    void verbose(String s);
    void verbose(Object o);
    void verbose(Throwable e);
    void warning(String s);
    void warning(Object o);
    void warning(Throwable e);
    void error(String s);
    void error(Object o);
    void error(Throwable e);

    static Logger getInstance(){
        if (INSTANCE != null) return INSTANCE;
        if (Constants.VERBOSE) return new VerboseLogger();
        if (Constants.DEBUG) return new DebugLogger();
        return new DefaultLogger();
    }
}
