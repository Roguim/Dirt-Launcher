package net.dirtcraft.dirtlauncher.backend.objects;

import net.cydhra.nidhogg.MojangClient;
import net.cydhra.nidhogg.data.Session;

import java.util.UUID;

public class Account {

    private Session session;
    private MojangClient mojangClient;
    private String username;
    private String password;
    private UUID uuid;
    private boolean isAuthenticated;

    public Account(Session session, MojangClient mojangClient, String username, String password, UUID uuid, boolean isAuthenticated) {
        this.session = session;
        this.mojangClient = mojangClient;
        this.username = username;
        this.password = password;
        this.uuid = uuid;
        this.isAuthenticated = isAuthenticated;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }
}
