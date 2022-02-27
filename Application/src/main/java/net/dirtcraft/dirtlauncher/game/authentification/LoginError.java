package net.dirtcraft.dirtlauncher.game.authentification;

import net.dirtcraft.dirtlauncher.gui.dialog.ErrorWindow;

public enum LoginError {

    INVALID_CREDENTIALS, INVALID_SESSION, TOO_MANY_REQUESTS, UNAUTHORISED_OPERATION, USER_MIGRATED, YGGDRASIL_BAN, ILLEGAL_ARGUMENT, UNKNOWN;

    public static LoginError from(Exception yggdrasilClientException){
        if (yggdrasilClientException instanceof IllegalArgumentException) {
            new ErrorWindow(yggdrasilClientException.getLocalizedMessage());
            return ILLEGAL_ARGUMENT;
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        switch (this){
            case INVALID_CREDENTIALS: return "Your E-Mail or password is invalid!";
            case INVALID_SESSION: return "Your session was invalid.";
            case TOO_MANY_REQUESTS: return "You have been sending too many requests. Please wait.";
            case UNAUTHORISED_OPERATION: return "This is an unauthorised Operation.";
            case USER_MIGRATED: return "Please use your E-Mail to log in!";
            case YGGDRASIL_BAN: return "You have been banned from the yggdrasil authentication server.";
            case ILLEGAL_ARGUMENT: return "Your username or password contains invalid arguments!";
            case UNKNOWN:
            default: return "There was an unexpected error!";
        }
    }
}
