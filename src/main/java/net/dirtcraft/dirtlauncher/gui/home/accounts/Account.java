package net.dirtcraft.dirtlauncher.gui.home.accounts;


import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.cydhra.nidhogg.YggdrasilAgent;
import net.cydhra.nidhogg.YggdrasilClient;
import net.cydhra.nidhogg.data.AccountCredentials;
import net.cydhra.nidhogg.data.Session;
import net.cydhra.nidhogg.exception.*;

import java.util.UUID;

public class Account{
    private Session session;
    private YggdrasilClient client;

    public Account(AccountCredentials credentials, YggdrasilClient client) throws InvalidCredentialsException, InvalidSessionException, TooManyRequestsException, UnauthorizedOperationException, UserMigratedException, YggdrasilBanException {
        this.client = client;
        session = client.login(credentials, YggdrasilAgent.MINECRAFT);
    }

    public Account(JsonObject jsonObject, YggdrasilClient client) {
        this.client = client;
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

    public JsonObject getSerialized(){
        if (session == null) return new JsonObject();
        final JsonObject json = new JsonObject();
        json.addProperty("UUID", session.getId());
        json.addProperty("Alias", session.getAlias());
        json.addProperty("AccessToken", session.getAccessToken());
        json.addProperty("ClientToken", session.getClientToken());
        return json;
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
            System.out.println("Session not valid, Attempting to refresh it!");
            try {
                client.refresh(session);
                return true;
            } catch (Exception refreshException){
                System.out.println(e.getMessage());
                System.out.println(refreshException.getMessage());
                System.out.println("Session not valid.");
            }
        }
        return false;
    }
}
