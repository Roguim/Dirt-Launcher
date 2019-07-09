package net.dirtcraft.dirtlauncher.backend.JsonUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.backend.objects.PackType;

import java.util.ArrayList;
import java.util.List;

public class Pack {


    private String name;
    private String version;
    private PackType packType;
    private String link;
    private String splash;
    private String logo;
    private String gameVersion;
    private int requiredRam;
    private int recommendedRam;
    private String forgeVersion;
    private List<OptionalMod> optionalMods;

    public Pack(JsonObject json) {
        this.name = json.get("name").getAsString();
        this.version = json.get("version").getAsString();
        this.packType = json.get("packType").getAsString().equalsIgnoreCase("CURSE") ? PackType.CURSE : PackType.CUSTOM;
        this.link = json.get("link").getAsString();
        this.splash = json.get("splash").getAsString();
        this.logo = json.get("logo").getAsString();
        this.gameVersion = json.get("gameVersion").getAsString();
        this.requiredRam = json.get("requiredRam").getAsInt();
        this.recommendedRam = json.get("recommendedRam").getAsInt();
        this.forgeVersion = json.get("forgeVersion").getAsString();

        List<OptionalMod> optionalMods = new ArrayList<>();
        for (JsonElement mods : json.get("optionalMods").getAsJsonArray()) {
            optionalMods.add(new OptionalMod(mods.getAsJsonObject()));
        }
        this.optionalMods = optionalMods;
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

    public PackType getPackType() {
        return packType;
    }

    public void setPackType(PackType packType) {
        this.packType = packType;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getSplash() {
        return splash;
    }

    public void setSplash(String splash) {
        this.splash = splash;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public void setGameVersion(String gameVersion) {
        this.gameVersion = gameVersion;
    }

    public int getRequiredRam() {
        return requiredRam;
    }

    public void setRequiredRam(int requiredRam) {
        this.requiredRam = requiredRam;
    }

    public int getRecommendedRam() {
        return recommendedRam;
    }

    public void setRecommendedRam(int recommendedRam) {
        this.recommendedRam = recommendedRam;
    }

    public String getForgeVersion() {
        return forgeVersion;
    }

    public void setForgeVersion(String forgeVersion) {
        this.forgeVersion = forgeVersion;
    }

    public List<OptionalMod> getOptionalMods() {
        return optionalMods;
    }

    public void setOptionalMods(List<OptionalMod> optionalMods) {
        this.optionalMods = optionalMods;
    }
}
