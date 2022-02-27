package net.dirtcraft.dirtlauncher.game.authentification;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AccountStorage {
    Set<Account> altAccounts;
    Account selectedAccount;
    String clientToken;

    AccountStorage(UUID uuid){
        altAccounts = new HashSet<>();
    }

    public void addAccount(Account account) {
        if (altAccounts == null) altAccounts = new HashSet<>();
        altAccounts.add(account);
    }

    public void removeAccount(Account account) {
        if (altAccounts == null) altAccounts = new HashSet<>();
        altAccounts.remove(account);
    }

    public void selectAccount(Account account) {
        if (altAccounts == null) altAccounts = new HashSet<>();
        altAccounts.add(account);
        selectedAccount = account;
    }

    public void removeSelected() {
        if (altAccounts == null) altAccounts = new HashSet<>();
        altAccounts.remove(selectedAccount);
        selectAccount(null);
    }

    public Set<Account> getAltAccounts() {
        return altAccounts;
    }
}
