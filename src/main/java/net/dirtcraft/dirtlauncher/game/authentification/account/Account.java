package net.dirtcraft.dirtlauncher.game.authentification.account;

import java.util.Objects;

public abstract class Account {

    public Account(AccountType type){
        this.type = type;
    }

    public final AccountType type;

    public abstract String getAlias();

    public abstract String getAccessToken();

    public abstract String getId();

    public abstract boolean isValid();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return getId().equals(account.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
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
