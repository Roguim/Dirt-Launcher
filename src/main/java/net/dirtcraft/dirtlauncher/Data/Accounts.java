package net.dirtcraft.dirtlauncher.Data;

import com.google.gson.*;
import net.cydhra.nidhogg.YggdrasilClient;
import net.cydhra.nidhogg.data.AccountCredentials;
import net.cydhra.nidhogg.exception.*;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.gui.home.accounts.Account;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.util.*;
import java.io.IOException;
import java.nio.file.Path;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.concurrent.CompletableFuture;

public final class Accounts {

    private Account selectedAccount;
    final private List<Account> altAccounts;
    private YggdrasilClient client = null;
    private final File accountDir;
    private volatile boolean isReady = false;
    private final String finalYggdrasilClientToken;


    public Accounts(Path launcherDirectory){
        boolean saveData = false;
        JsonObject accounts;
        accountDir = launcherDirectory.resolve("account.json").toFile();
        try (FileReader reader = new FileReader(accountDir)) {
            JsonParser parser = new JsonParser();
            accounts = parser.parse(reader).getAsJsonObject();
        } catch (IOException e){
            accounts = null;
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
            client = new YggdrasilClient(finalYggdrasilClientToken);
            isReady = true;
        });

        try {
            if (accounts != null && accounts.has("selected account")) {
                try {
                    selectedAccount = new Account(accounts.getAsJsonObject("selected account"), client);
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

        altAccounts = new ArrayList<>();
        if (accounts != null && accounts.has("alt account list")) {
            for (JsonElement entry : accounts.getAsJsonArray("alt account list")){
                try {
                    final Account session = new Account(entry.getAsJsonObject(), client);
                } catch (JsonParseException e) {
                    System.out.println(e.getMessage());
                }
            }
        } else {
            System.out.println("No Alternate Account List detected");
        }

        if (saveData) saveData();
    }

    private void saveData(){
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
            Main.getLogger().warn(e);
        }
    }

    public void setSelectedAccount(Account newAccount) {
        if (newAccount.isValid()){
            if (selectedAccount != null) altAccounts.add(selectedAccount);
            altAccounts.remove(newAccount);
            selectedAccount = newAccount;
            saveData();
        } else clearSelectedAccount();
    }

    public void setSelectedAccount(AccountCredentials credentials) throws InvalidCredentialsException, InvalidSessionException, TooManyRequestsException, UnauthorizedOperationException, UserMigratedException, YggdrasilBanException {
        selectedAccount = new Account(credentials, client);
        saveData();
    }

    public void clearSelectedAccount(){
        if (selectedAccount != null) altAccounts.add(selectedAccount);
        selectedAccount = null;
        saveData();
    }

    public List<Account> getAltAccounts() {
        altAccounts.removeIf(session -> !session.isValid());
        return altAccounts;
    }

    public boolean hasSelectedAccount(){
        return selectedAccount != null;
    }

    public Optional<Account> getSelectedAccount() {
        if (selectedAccount == null) return Optional.empty();
        else return Optional.of(selectedAccount);
    }

    private YggdrasilClient getClient() {
        return client;
    }

    public boolean isReady() {
        return isReady;
    }
}
