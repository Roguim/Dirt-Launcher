package net.dirtcraft.dirtlauncher.game.authentification;

import javafx.application.Platform;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.configuration.ConfigBase;
import net.dirtcraft.dirtlauncher.gui.dialog.LoginDialogueMicrosoft;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class AccountManager extends ConfigBase<AccountStorage> {

    public AccountManager(Path launcherDirectory){
        super(launcherDirectory.resolve("account.json").toFile(), AccountStorage.class, ()->new AccountStorage(UUID.randomUUID()));
        load();
        verifySelected();
    }

    public void verifySelected() {
        if (configBase.selectedAccount == null || !configBase.selectedAccount.isValid()) {
            configBase.selectedAccount = null;
        }
    }

    public void setSelectedAccount(Account newAccount) {
        configBase.selectAccount(newAccount);
        saveAsync();
    }

    public void logout(){
        configBase.selectAccount(null);
        saveAsync();
    }

    public void login()  {
        LoginDialogueMicrosoft.grabToken(token->{
            configBase.selectAccount(new Account(token));
            saveAsync();
            Platform.runLater(()-> Main.getHome().getLoginBar().setInputs());
        });
    }

    public Set<Account> getAltAccounts() {
        return configBase.getAltAccounts();
    }

    public boolean hasSelectedAccount(){
        return configBase.selectedAccount != null;
    }

    public Optional<Account> getSelectedAccount() {
        return Optional.ofNullable(configBase.selectedAccount);
    }

    public Account getSelectedAccountUnchecked() {
        return configBase.selectedAccount;
    }

    @Override
    public void load(){
        configBase = JsonUtils.parseJson(configFile, type).orElse(new AccountStorage(UUID.randomUUID()));
    }
}
