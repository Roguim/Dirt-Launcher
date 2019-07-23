package net.dirtcraft.dirtlauncher.backend.objects;

import net.cydhra.nidhogg.data.Session;

import java.util.UUID;

public class Account {

    private Session session;
    private String username;
    private UUID uuid;
    private boolean isAuthenticated;

    public Account(Session session, String username, UUID uuid, boolean isAuthenticated) {
        this.session = session;
        this.username = username;
        this.uuid = uuid;
        this.isAuthenticated = isAuthenticated;
    }

    public String getUsername() {
        return username;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public Session getSession() {
        return session;
    }
}
