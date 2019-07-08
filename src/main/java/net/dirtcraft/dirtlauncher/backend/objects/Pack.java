package net.dirtcraft.dirtlauncher.backend.objects;

import com.google.api.client.util.Key;

import java.util.List;

public class Pack {

    @Key
    private String name;
    @Key
    private String version;
    @Key
    private PackType packType;
    @Key
    private String link;
    @Key
    private String splash;
    @Key
    private String logo;
    @Key
    private String gameVersion;
    @Key
    private int requiredRam;
    @Key
    private int recommendedRam;
    @Key
    private String forgeVersion;
    @Key
    private List<OptionalMod> optionalMods;

    public Pack(String name, String version, PackType packType, String link, String splash, String logo, String gameVersion, int requiredRam, int recommendedRam, String forgeVersion, List<OptionalMod> optionalMods) {
        this.name = name;
        this.version = version;
        this.packType = packType;
        this.link = link;
        this.splash = splash;
        this.logo = logo;
        this.gameVersion = gameVersion;
        this.requiredRam = requiredRam;
        this.recommendedRam = recommendedRam;
        this.forgeVersion = forgeVersion;
        this.optionalMods = optionalMods;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public PackType getPackType() {
        return packType;
    }

    public String getLink() {
        return link;
    }

    public String getSplash() {
        return splash;
    }

    public String getLogo() {
        return logo;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public int getRequiredRam() {
        return requiredRam;
    }

    public int getRecommendedRam() {
        return recommendedRam;
    }

    public String getForgeVersion() {
        return forgeVersion;
    }

    public List<OptionalMod> getOptionalMods() {
        return optionalMods;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setPackType(PackType packType) {
        this.packType = packType;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setSplash(String string) {
        this.splash = splash;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public void setGameVersion(String gameVersion) {
        this.gameVersion = gameVersion;
    }

    public void setRequiredRam(int requiredRam) {
        this.requiredRam = requiredRam;
    }

    public void setRecommendedRam(int recommendedRam) {
        this.recommendedRam = recommendedRam;
    }

    public void setForgeVersion(String forgeVersion) {
        this.forgeVersion = forgeVersion;
    }

    public void setOptionalMods(List<OptionalMod> optionalMods) {
        this.optionalMods = optionalMods;
    }

}
