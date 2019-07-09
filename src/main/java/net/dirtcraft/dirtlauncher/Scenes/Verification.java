package net.dirtcraft.dirtlauncher.Scenes;

import net.cydhra.nidhogg.MojangClient;
import net.cydhra.nidhogg.YggdrasilAgent;
import net.cydhra.nidhogg.YggdrasilClient;
import net.cydhra.nidhogg.data.AccountCredentials;
import net.cydhra.nidhogg.data.NameEntry;
import net.cydhra.nidhogg.data.Session;
import net.cydhra.nidhogg.exception.InvalidCredentialsException;
import net.dirtcraft.dirtlauncher.backend.objects.Account;

import java.util.List;
import java.util.UUID;

public class Verification {

    public static Account login(String email, String password) throws InvalidCredentialsException {
        if (email == null || password == null) throw new InvalidCredentialsException("Dude! You forgot to put in your email or password!");

        YggdrasilClient client = new YggdrasilClient();

        Session session = client.login(new AccountCredentials(email, password), YggdrasilAgent.MINECRAFT);
        final UUID uuid = session.getUuid();

        MojangClient mojangClient = new MojangClient(session.getClientToken());

        List<NameEntry> names = mojangClient.getNameHistoryByUUID(uuid);

        Account account = new Account(names.get(names.size() - 1).getName(), password, uuid, client.validate(session));

        System.out.println("USERNAME: " + account.getUsername());
        System.out.println("PASSWORD: " + account.getPassword());
        System.out.println("UUID: " + account.getUuid());
        System.out.println("IS AUTHENTICATED: " + account.isAuthenticated());

        return account;

    }

}
