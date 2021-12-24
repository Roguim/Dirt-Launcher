package net.dirtcraft.dirtlauncher.game.authentification.account;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.cydhra.nidhogg.YggdrasilAgent;
import net.cydhra.nidhogg.data.AccountCredentials;
import net.cydhra.nidhogg.data.Session;
import net.cydhra.nidhogg.exception.*;
import net.dirtcraft.dirtlauncher.Main;

public class LegacyAccount extends Account {
    private Session session;

    public static LegacyAccount login(String email, String password) throws InvalidCredentialsException, InvalidSessionException, TooManyRequestsException, UnauthorizedOperationException, UserMigratedException, YggdrasilBanException {
        return new LegacyAccount(new AccountCredentials(email, password));
    }

    public LegacyAccount(AccountCredentials credentials) throws InvalidCredentialsException, InvalidSessionException, TooManyRequestsException, UnauthorizedOperationException, UserMigratedException, YggdrasilBanException {
        this.session = Main.getAccounts().getClient().login(credentials, YggdrasilAgent.MINECRAFT);
    }

    public LegacyAccount(JsonObject jsonObject) {
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
