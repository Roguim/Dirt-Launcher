package net.dirtcraft.dirtlauncher.game.modpacks;

import com.google.gson.JsonObject;

public class OptionalMod {

    private String name;
    private String version;
    private String link;
    private String description;


    public OptionalMod(JsonObject json) {
        this.name = json.get("name").toString();
        this.version = json.get("version").toString();
        this.link = json.get("link").toString();
        this.description = json.get("description").toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
