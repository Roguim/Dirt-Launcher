package net.dirtcraft.dirtlauncher.game.installation.tasks;

public class PackInstallException extends RuntimeException {
    public PackInstallException(String message, Throwable error) {
        super(message, error);
    }

    public PackInstallException(String message) {
        super(message);
    }

    public PackInstallException(Exception e) {
        super(e);
    }
}
