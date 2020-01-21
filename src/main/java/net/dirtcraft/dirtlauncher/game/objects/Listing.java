package net.dirtcraft.dirtlauncher.game.objects;

import com.google.gson.JsonObject;

import java.util.Optional;

public class Listing {

    private String name;
    private String ip;
    private String icon;

    public Listing(String name, String ip) {
        this.name = name;
        this.ip = ip;
    }

    public Listing(String name, String ip, String icon) {
        this.name = name;
        this.ip = ip;
        this.icon = icon;
    }

    public Listing(JsonObject json) {
        this.name = json.get("name").getAsString().replace("&", "§");
        this.ip = json.get("ip").getAsString();
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public Optional<String> getIcon(){
        return Optional.ofNullable(icon);
    }
}
