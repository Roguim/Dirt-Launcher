package net.dirtcraft.dirtlauncher.game.authentification.account;

public abstract class Account {

    public abstract String getAlias();

    public abstract String getAccessToken();

    public abstract String getId();

    public abstract boolean isValid(boolean save);

    public boolean isValid() {
        return isValid(true);
    }
}
