package net.dirtcraft.dirtlauncher.game.authentification;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.cydhra.nidhogg.YggdrasilAgent;
import net.cydhra.nidhogg.data.AccountCredentials;
import net.cydhra.nidhogg.data.Session;
import net.cydhra.nidhogg.exception.*;
import net.dirtcraft.dirtlauncher.Main;

//import java.util.UUID;

public class Account{
    private Session session;

    Account(AccountCredentials credentials) throws InvalidCredentialsException, InvalidSessionException, TooManyRequestsException, UnauthorizedOperationException, UserMigratedException, YggdrasilBanException {
        this.session = Main.getAccounts().getClient().login(credentials, YggdrasilAgent.MINECRAFT);
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

    public String getAlias(){
        return session.getAlias();
    }

    public String getAccessToken(){
        return session.getAccessToken();
    }

    public String getId(){
        return session.getId();
    }

    public boolean isValid(){
        try {
            Main.getAccounts().getClient().validate(session);
            return true;
        } catch (Exception e){
            System.out.println("Session not valid, Attempting to refresh it!");
            try {
                Main.getAccounts().getClient().refresh(session);
                Main.getAccounts().saveAsync();
                return true;
            } catch (Exception refreshException){
                System.out.println(e.getMessage());
                System.out.println(refreshException.getMessage());
                System.out.println("Session not valid.");
            }
        }
        return false;
    }

    public boolean isValid(boolean save){
        try {
            Main.getAccounts().getClient().validate(session);
            return true;
        } catch (Exception e){
            System.out.println("Session not valid, Attempting to refresh it!");
            try {
                Main.getAccounts().getClient().refresh(session);
                if (save) Main.getAccounts().saveAsync();
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
