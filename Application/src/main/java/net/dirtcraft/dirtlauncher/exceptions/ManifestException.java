package net.dirtcraft.dirtlauncher.exceptions;

public class ManifestException extends LauncherException {
    public ManifestException(){}

    public ManifestException(String message) {
        super(message);
    }

    public ManifestException(Exception e) {
        super(e);
    }
}
