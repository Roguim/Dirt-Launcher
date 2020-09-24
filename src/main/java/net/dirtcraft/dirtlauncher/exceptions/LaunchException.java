package net.dirtcraft.dirtlauncher.exceptions;

public class LaunchException extends LauncherException {
    final String message;
    public LaunchException(String message){
        this.message = message;
    }

    @Override
    public String getMessage(){
        return message;
    }
}
