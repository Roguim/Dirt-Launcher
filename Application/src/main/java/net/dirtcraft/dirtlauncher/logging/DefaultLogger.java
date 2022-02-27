package net.dirtcraft.dirtlauncher.logging;

public class DefaultLogger implements Logger {

    @Override
    public void info(String s) {
        System.out.println(s);
    }

    @Override
    public void info(Object o) {

    }

    @Override
    public void debug(String s) {
    }

    @Override
    public void debug(Object o) {

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

    }

    @Override
    public void warning(Object o) {

    }

    @Override
    public void warning(Throwable e) {

    }

    @Override
    public void error(String s) {
        System.out.println("ERROR:");
        System.out.println(s);
    }

    @Override
    public void error(Object o) {
        System.out.println(o);
    }

    @Override
    public void error(Throwable e) {
        e.printStackTrace();
    }
}
