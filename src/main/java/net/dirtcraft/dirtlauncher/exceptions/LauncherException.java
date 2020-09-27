package net.dirtcraft.dirtlauncher.exceptions;

public class LauncherException extends Exception {

    public LauncherException(){}

    public LauncherException(String message) {
        super(message);
    }

    public LauncherException(Exception e) {
        super(e);
    }
}
