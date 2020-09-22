package net.dirtcraft.dirtlauncher.game.authentification;

import net.cydhra.nidhogg.data.AccountCredentials;
import net.cydhra.nidhogg.exception.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AccountStorage {
    List<Account> altAccounts;
    Account selectedAccount;
    String clientToken;

    public boolean isValid(){
        return altAccounts != null && clientToken != null;
    }

    AccountStorage(UUID uuid){
        altAccounts = new ArrayList<>();
        selectedAccount = null;
        clientToken = uuid.toString().replaceAll("-", "");
    }

    public void removeSelectedAccount(){
        if (selectedAccount != null) altAccounts.add(selectedAccount);
        selectedAccount = null;
    }

    public boolean setSelectedAccount(Account newAccount) {
        if (!newAccount.isValid()) return false;
        if (selectedAccount != null) altAccounts.add(selectedAccount);
        altAccounts.remove(newAccount);
        selectedAccount = newAccount;
        return true;
    }

    public void setSelectedAccount(AccountCredentials credentials) throws InvalidCredentialsException, InvalidSessionException, TooManyRequestsException, UnauthorizedOperationException, UserMigratedException, YggdrasilBanException {
        selectedAccount = new Account(credentials);
        altAccounts.removeIf(account -> account.getId().equals(selectedAccount.getId()));
    }

    public void refreshSelectedAccount(){
        if (selectedAccount != null && !selectedAccount.isValid()) selectedAccount = null;
    }

    public List<Account> getAltAccounts() {
        return altAccounts;
    }

    public void trimInvalidAlts(){
        altAccounts.removeIf(session -> !session.isValid(false));
    }
}
