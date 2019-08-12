package net.dirtcraft.dirtlauncher.game.objects;

import com.google.gson.JsonObject;

public class Listing {

    private String name;
    private String ip;

    public Listing(String name, String ip) {
        this.name = name;
        this.ip = ip;
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
}
