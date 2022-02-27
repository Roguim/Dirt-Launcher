package net.dirtcraft.dirtlauncher.logging;

public class DebugLogger implements Logger {
    {
        System.out.println("Debug Level Logging Enabled!");
    }

    @Override
    public void info(String s) {
        System.out.println(s);
    }

    @Override
    public void info(Object o) {
        System.out.println(o);
    }

    @Override
    public void debug(String s) {
        System.out.println(s);
    }

    @Override
    public void debug(Object o) {
        System.out.println(o);
    }

    @Override
    public void verbose(String s) {
    }

    @Override
    public void verbose(Object o) {
    }

    @Override
    public void verbose(Throwable e) {
    }

    @Override
    public void warning(String s) {
        System.out.println(s);
    }

    @Override
    public void warning(Object o) {
        System.out.println(o);
    }

    @Override
    public void warning(Throwable e) {
        System.out.println("WARNING:");
        e.printStackTrace();
    }

    @Override
    public void error(String s) {
        System.out.println(s);
    }

    @Override
    public void error(Object o) {
        System.out.println(o);
    }

    @Override
    public void error(Throwable e) {
        System.out.println("ERROR:");
        e.printStackTrace();
    }
}
