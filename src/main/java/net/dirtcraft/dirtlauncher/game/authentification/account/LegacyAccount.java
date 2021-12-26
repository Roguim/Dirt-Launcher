package net.dirtcraft.dirtlauncher.game.authentification.account;

import net.cydhra.nidhogg.YggdrasilAgent;
import net.cydhra.nidhogg.data.AccountCredentials;
import net.cydhra.nidhogg.data.Session;
import net.cydhra.nidhogg.exception.*;
import net.dirtcraft.dirtlauncher.DirtLauncher;

public class LegacyAccount extends Account {
    private Session session;

    public static LegacyAccount login(String email, String password) throws InvalidCredentialsException, InvalidSessionException, TooManyRequestsException, UnauthorizedOperationException, UserMigratedException, YggdrasilBanException {
        return new LegacyAccount(new AccountCredentials(email, password));
    }

    public LegacyAccount(AccountCredentials credentials) throws InvalidCredentialsException, InvalidSessionException, TooManyRequestsException, UnauthorizedOperationException, UserMigratedException, YggdrasilBanException {
        super(AccountType.MOJANG);
        this.session = DirtLauncher.getAccounts().getClient().login(credentials, YggdrasilAgent.MINECRAFT);
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
            DirtLauncher.getAccounts().getClient().validate(session);
            return true;
        } catch (Exception e){
            System.out.println("Session not valid, Attempting to refresh it!");
            try {
                DirtLauncher.getAccounts().getClient().refresh(session);
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
