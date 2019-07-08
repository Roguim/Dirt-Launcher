package net.dirtcraft.dirtlauncher.backend.objects;

import com.google.api.client.util.Key;

public class OptionalMod {

    @Key
    private String name;
    @Key
    private String version;
    @Key
    private String link;
    @Key
    private String description;

    public OptionalMod(String name, String version, String link, String description) {
        this.name = name;
        this.version = version;
        this.link = link;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getLink() {
        return link;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
