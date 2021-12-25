package net.dirtcraft.dirtlauncher.game.authentification.account;

public abstract class Account {

    public Account(AccountType type){
        this.type = type;
    }

    public final AccountType type;

    public abstract String getAlias();

    public abstract String getAccessToken();

    public abstract String getId();

    public abstract boolean isValid(boolean save);

    public boolean isValid() {
        return isValid(true);
    }


    public enum AccountType {
        MICROSOFT   (MicroAccount.class),
        MOJANG      (LegacyAccount.class);
        public final Class<? extends Account> type;
        AccountType(Class<? extends Account> type){
            this.type = type;
        }
    }
}
