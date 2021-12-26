package net.dirtcraft.dirtlauncher.game.authentification;

import net.cydhra.nidhogg.YggdrasilClient;
import net.cydhra.nidhogg.exception.*;
import net.dirtcraft.dirtlauncher.configuration.ConfigBase;
import net.dirtcraft.dirtlauncher.game.authentification.account.Account;
import net.dirtcraft.dirtlauncher.game.authentification.account.LegacyAccount;
import net.dirtcraft.dirtlauncher.game.authentification.account.MicroAccount;
import net.dirtcraft.dirtlauncher.gui.dialog.LoginDialogueMicrosoft;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public final class AccountManager extends ConfigBase<AccountStorage> {

    private YggdrasilClient client;

    public AccountManager(Path launcherDirectory){
        super(launcherDirectory.resolve("account.json").toFile(), AccountStorage.class, ()->new AccountStorage(UUID.randomUUID()));
        client = new YggdrasilClient();
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

    public void login() throws InvalidCredentialsException, InvalidSessionException, TooManyRequestsException, UnauthorizedOperationException, UserMigratedException, YggdrasilBanException {
        LoginDialogueMicrosoft.grabToken(token->{
            configBase.selectAccount(new MicroAccount(token));
            saveAsync();
        });
    }

    public void login(String email, String password, Consumer<Exception> onFailure) {
        try {
            configBase.selectAccount(LegacyAccount.login(email, password));
            saveAsync();
        } catch (Exception e) {
            onFailure.accept(e);
        }
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

    public YggdrasilClient getClient() {
        return client;
    }

    @Override
    public void load(){
        configBase = JsonUtils.parseJson(configFile, type).orElse(new AccountStorage(UUID.randomUUID()));
    }
}
