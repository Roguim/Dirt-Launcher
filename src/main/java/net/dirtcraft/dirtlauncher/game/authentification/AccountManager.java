package net.dirtcraft.dirtlauncher.game.authentification;

import com.google.gson.*;
import net.cydhra.nidhogg.YggdrasilClient;
import net.cydhra.nidhogg.data.AccountCredentials;
import net.cydhra.nidhogg.exception.*;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class AccountManager {

    private Account selectedAccount;
    final private List<Account> altAccounts;
    private YggdrasilClient client = null;
    private final File accountDir;
    private volatile boolean isReady = false;
    private final String finalYggdrasilClientToken;


    public AccountManager(Path launcherDirectory){
        boolean saveData = false;
        JsonObject accounts;
        accountDir = launcherDirectory.resolve("account.json").toFile();
        try (FileReader reader = new FileReader(accountDir)) {
            JsonParser parser = new JsonParser();
            accounts = parser.parse(reader).getAsJsonObject();
        } catch (IOException e){
            accounts = null;
        }

        try {
            if (accounts != null && accounts.has("selected account")) {
                try {
                    selectedAccount = new Account(accounts.getAsJsonObject("selected account"));
                } catch (JsonParseException e) {
                    System.out.println(e.getMessage());
                }
            } else {
                throw new JsonParseException("No Selected Account");
            }
        } catch (JsonParseException e){
            System.out.println(e.getMessage());
            selectedAccount = null;
        }

        String yggdrasilClientToken;
        try {
            if (accounts != null && accounts.has("client token")) {
                yggdrasilClientToken = accounts.get("client token").getAsString();
            } else {
                throw new JsonParseException("No Selected Account");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            byte[] bytes = new byte[16];
            new Random().nextBytes(bytes);
            yggdrasilClientToken = DatatypeConverter.printHexBinary(bytes);
            saveData = true;
        }
        finalYggdrasilClientToken = yggdrasilClientToken;
        CompletableFuture.runAsync(()->{
            long x = System.currentTimeMillis();
            client = new YggdrasilClient(finalYggdrasilClientToken);
            System.out.println("nidhogg took " + (System.currentTimeMillis() - x));
            isReady = true;
        });

        altAccounts = new ArrayList<>();
        if (accounts != null && accounts.has("alt account list")) {
            for (JsonElement entry : accounts.getAsJsonArray("alt account list")){
                try {
                    final Account session = new Account(entry.getAsJsonObject());
                    altAccounts.add(session);
                } catch (JsonParseException e) {
                    System.out.println(e.getMessage());
                }
            }
        } else {
            System.out.println("No Alternate Account List detected");
        }

        if (saveData) saveData();
    }

    public synchronized void saveData(){
        final JsonObject accounts = new JsonObject();
        final JsonArray alts = new JsonArray();
        for (Account session : altAccounts){
            alts.add(session.getSerialized());
        }
        if (selectedAccount != null) accounts.add("selected account", selectedAccount.getSerialized());
        accounts.add("alt account list", alts);
        accounts.addProperty("client token", finalYggdrasilClientToken);
        try (FileWriter writer = new FileWriter(accountDir)) {
            writer.write(accounts.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSelectedAccount(Account newAccount) {
        if (newAccount.isValid()){
            if (selectedAccount != null) altAccounts.add(selectedAccount);
            altAccounts.remove(newAccount);
            selectedAccount = newAccount;
            saveData();
        } else logout();
    }

    public void setSelectedAccount(AccountCredentials credentials) throws InvalidCredentialsException, InvalidSessionException, TooManyRequestsException, UnauthorizedOperationException, UserMigratedException, YggdrasilBanException {
        selectedAccount = new Account(credentials);
        altAccounts.removeIf(account -> account.getId().equals(selectedAccount.getId()));
        saveData();
    }

    public void logout(){
        if (selectedAccount != null) altAccounts.add(selectedAccount);
        selectedAccount = null;
        saveData();
    }

    public void login(String email, String password, Consumer<Exception> onFailure) {
        try {
            setSelectedAccount(new AccountCredentials(email, password));
        } catch (Exception e) {
            onFailure.accept(e);
        }
    }

    public List<Account> getAltAccounts() {
        altAccounts.removeIf(session -> !session.isValid(false));
        CompletableFuture.runAsync(this::saveData);
        return altAccounts;
    }

    public boolean hasSelectedAccount(){
        return selectedAccount != null;
    }

    public Optional<Account> getSelectedAccount() {
        if (selectedAccount == null) return Optional.empty();
        else return Optional.of(selectedAccount);
    }

    public Account getSelectedAccountUnchecked() {
        return selectedAccount;
    }

    public YggdrasilClient getClient() {
        return client;
    }

    public void refreshSelectedAccount(){
        CompletableFuture.runAsync(()->{
            while(!isReady){
                try {
                    Thread.sleep(50);
                } catch (Exception ignored) {}
            }
            if (!selectedAccount.isValid()) selectedAccount = null;
        });
    }

    public boolean isReady() {
        return isReady;
    }
}
