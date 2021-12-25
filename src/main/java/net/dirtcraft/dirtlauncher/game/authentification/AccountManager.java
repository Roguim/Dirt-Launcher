package net.dirtcraft.dirtlauncher.game.authentification;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import net.cydhra.nidhogg.YggdrasilClient;
import net.cydhra.nidhogg.exception.*;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.configuration.ConfigBase;
import net.dirtcraft.dirtlauncher.game.authentification.account.Account;
import net.dirtcraft.dirtlauncher.game.authentification.account.LegacyAccount;
import net.dirtcraft.dirtlauncher.game.authentification.account.MicroAccount;
import net.dirtcraft.dirtlauncher.gui.dialog.LoginDialogueMicrosoft;
import net.dirtcraft.dirtlauncher.logging.Logger;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class AccountManager extends ConfigBase<AccountStorage> {

    private YggdrasilClient client;

    public AccountManager(Path launcherDirectory){
        super(launcherDirectory.resolve("account.json").toFile(), AccountStorage.class, ()->new AccountStorage(UUID.randomUUID()));
        client = new YggdrasilClient();
        load();
    }

    public void setSelectedAccount(Account newAccount) {
        if (!configBase.setSelectedAccount(newAccount)) logout();
        saveAsync();
    }

    public void logout(){
        configBase.removeSelectedAccount();
        saveAsync();
    }

    public void login() throws InvalidCredentialsException, InvalidSessionException, TooManyRequestsException, UnauthorizedOperationException, UserMigratedException, YggdrasilBanException {
        LoginDialogueMicrosoft.grabToken(token->{
            MicroAccount account = new MicroAccount(token);
            configBase.setSelectedAccount(account);
            saveAsync();
        });
    }

    public void login(String email, String password, Consumer<Exception> onFailure) {
        login(email, password, onFailure, true);
    }

    public void login(String email, String password, Consumer<Exception> onFailure, boolean ms) {
        try {
            final Account account;
            account = LegacyAccount.login(email, password);
            configBase.setSelectedAccount(account);
            saveAsync();
        } catch (Exception e) {
            onFailure.accept(e);
        }
    }

    public List<Account> getAltAccounts() {
        configBase.trimInvalidAlts();
        saveAsync();
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

    public CompletableFuture<Void> refreshSelectedAccount(){
        return CompletableFuture.runAsync(()->{
            while(!saveLock.tryAcquire()){
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                    Logger.INSTANCE.error(e);
                }
            }
            try {
                configBase.refreshSelectedAccount();
            } catch (Exception e){
                Logger.INSTANCE.error(e);
            }
        }).whenComplete((v,e)->{
            saveLock.release();
            saveAsync();
        });
    }

    @Override
    public void load(){
        configBase = JsonUtils.parseJson(configFile, type).orElse(new AccountStorage(UUID.randomUUID()));
    }
}
