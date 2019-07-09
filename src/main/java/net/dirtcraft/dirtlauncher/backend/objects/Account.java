package net.dirtcraft.dirtlauncher.backend.objects;

import java.util.UUID;

public class Account {

    private String username;
    private String password;
    private UUID uuid;
    private boolean isAuthenticated;

    public Account(String username, String password, UUID uuid, boolean isAuthenticated) {
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
