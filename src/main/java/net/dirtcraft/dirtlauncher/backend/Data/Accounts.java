package net.dirtcraft.dirtlauncher.backend.Data;

import com.google.gson.*;
import net.cydhra.nidhogg.YggdrasilAgent;
import net.cydhra.nidhogg.YggdrasilClient;
import net.cydhra.nidhogg.data.AccountCredentials;
import net.cydhra.nidhogg.data.Session;
import net.dirtcraft.dirtlauncher.Main;

import java.io.File;
import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.nio.file.Path;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class Accounts {

    private Session selectedAccount;
    final private List<Session> altAccounts;
    private YggdrasilClient client = null;
    private final File accountDir;
    private volatile boolean isReady = false;

    public Accounts(Path launcherDirectory){
        CompletableFuture.runAsync(()->{
            client = new YggdrasilClient();
            isReady = true;
        });
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
                selectedAccount = jsonToSession(accounts.getAsJsonObject("selected account"));
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
                final Session session = jsonToSession(entry.getAsJsonObject());
                if (session != null) altAccounts.add(session);
            }
        } else {
            System.out.println("No Alternate Account List detected");
        }

        //Legacy format pasting. remove this later
        if (altAccounts.isEmpty() && selectedAccount == null && accounts != null) {
            if (!accounts.has("sessionID")) throw new JsonParseException("No sessionID");
            if (!accounts.has("sessionAlias")) throw new JsonParseException("No sessionAlias");
            if (!accounts.has("sessionAccessToken")) throw new JsonParseException("No sessionAccessToken");
            if (!accounts.has("sessionClientToken")) throw new JsonParseException("No sessionClientToken");

            String sessionID = accounts.get("sessionID").getAsString();
            String sessionAlias = accounts.get("sessionAlias").getAsString();
            String sessionAccessToken = accounts.get("sessionAccessToken").getAsString();
            String sessionClientToken = accounts.get("sessionClientToken").getAsString();

            selectedAccount = new Session(sessionID, sessionAlias, sessionAccessToken, sessionClientToken);
            saveData();
        }
    }

    private void saveData(){
        final JsonObject accounts = new JsonObject();
        final JsonObject selected = sessionToJson(selectedAccount);
        final JsonArray alts = new JsonArray();
        for (Session session : altAccounts){
            alts.add(sessionToJson(session));
        }
        accounts.add("selected account", selected);
        accounts.add("alt account list", alts);
        try (FileWriter writer = new FileWriter(accountDir)) {
            writer.write(accounts.toString());
        } catch (IOException e) {
            Main.getLogger().warn(e);
        }
    }

    private JsonObject sessionToJson(Session session){
        if (session == null) return new JsonObject();
        final JsonObject json = new JsonObject();
        json.addProperty("UUID", session.getId());
        json.addProperty("Alias", session.getAlias());
        json.addProperty("AccessToken", session.getAccessToken());
        json.addProperty("ClientToken", session.getClientToken());
        return json;
    }

    private Session jsonToSession(JsonObject jsonObject){
        try {
            if (!jsonObject.has("UUID")) throw new JsonParseException("No UUID");
            if (!jsonObject.has("Alias")) throw new JsonParseException("No Alias");
            if (!jsonObject.has("AccessToken")) throw new JsonParseException("No Access Token");
            if (!jsonObject.has("ClientToken")) throw new JsonParseException("No Client Token");
            return new Session(
                    jsonObject.get("UUID").getAsString(),
                    jsonObject.get("Alias").getAsString(),
                    jsonObject.get("AccessToken").getAsString(),
                    jsonObject.get("ClientToken").getAsString()
            );
        } catch (JsonParseException e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    private boolean isValid(Session session) {
        if (session != null) {
            try {
                if (client.validate(session)) return true;
            } catch (Exception ex) {
                System.out.println("Session not valid, Attempting to refresh it!");
                try {
                    client.refresh(session);
                    return true;
                } catch (Exception e) {
                    System.out.println(ex.getMessage());
                    System.out.println(e.getMessage());
                }
            }
        }
        return false;
    }

    public void setSelectedAccount(Session newAccount) {
        if (isValid(newAccount)){
            if (selectedAccount != null) altAccounts.add(selectedAccount);
            altAccounts.remove(newAccount);
            selectedAccount = newAccount;
            saveData();
        } else clearSelectedAccount();
    }

    public void setSelectedAccount(AccountCredentials credentials){
        selectedAccount = client.login(credentials, YggdrasilAgent.MINECRAFT);
        saveData();
    }

    public void clearSelectedAccount(){
        if (selectedAccount != null) altAccounts.add(selectedAccount);
        selectedAccount = null;
        saveData();
    }

    public List<Session> getAltAccounts() {
        altAccounts.removeIf(session -> !isValid(session));
        return altAccounts;
    }

    public boolean hasSelectedAccount(){
        return selectedAccount != null;
    }

    public Optional<Session> getValidSelectedAccount() {
        if (selectedAccount == null) return Optional.empty();
        if (!isValid(selectedAccount)) return Optional.empty();
        else return Optional.of(selectedAccount);
    }

    public Optional<Session> getSelectedAccount() {
        if (selectedAccount == null) return Optional.empty();
        else return Optional.of(selectedAccount);
    }

    private YggdrasilClient getClient() {
        return client;
    }

    public boolean isReady() {
        return isReady;
    }

    private class Account{
        private Session session;
        Account(Session session){
            this.session = session;
        }

        Account(JsonObject jsonObject) {
            if (!jsonObject.has("UUID")) throw new JsonParseException("No UUID");
            if (!jsonObject.has("Alias")) throw new JsonParseException("No Alias");
            if (!jsonObject.has("AccessToken")) throw new JsonParseException("No Access Token");
            if (!jsonObject.has("ClientToken")) throw new JsonParseException("No Client Token");
            this.session = new Session(
                    jsonObject.get("UUID").getAsString(),
                    jsonObject.get("Alias").getAsString(),
                    jsonObject.get("AccessToken").getAsString(),
                    jsonObject.get("ClientToken").getAsString()
            );
        }

        public Account(String uuid, String name, String accessToken, String clientToken){
            this.session = new Session(uuid, name, accessToken, clientToken);
        }

        JsonObject getSerialized(){
            if (session == null) return new JsonObject();
            final JsonObject json = new JsonObject();
            json.addProperty("UUID", session.getId());
            json.addProperty("Alias", session.getAlias());
            json.addProperty("AccessToken", session.getAccessToken());
            json.addProperty("ClientToken", session.getClientToken());
            return json;
        }

        Session getSession() {
            return session;
        }

        public String getAlias(){
            return session.getAlias();
        }

        public String getAccessToken(){
            return session.getAccessToken();
        }

        public String getId(){
            return session.getId();
        }

        public String getClientToken(){
            return session.getId();
        }

        public UUID getUuid(){
            return session.getUuid();
        }

        public boolean isValid(){
            try {
                client.validate(session);
                return true;
            } catch (Exception e){
                try {
                    client.refresh(session);
                    return true;
                } catch (Exception refreshException){
                    throw e;
                }
            }
        }
    }
}
