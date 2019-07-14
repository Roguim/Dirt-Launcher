package net.dirtcraft.dirtlauncher.backend.objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.backend.config.Directories;
import net.dirtcraft.dirtlauncher.backend.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Pack {


    private String name;
    private String version;
    private String code;
    private PackType packType;
    private String link;
    private String splash;
    private String logo;
    private String gameVersion;
    private int requiredRam;
    private int recommendedRam;
    private String forgeVersion;
    private List<OptionalMod> optionalMods;
    private Optional<Integer> fileSize;

    public Pack(JsonObject json) {
        this.name = json.get("name").getAsString();
        this.version = json.get("version").getAsString();
        this.code = json.get("code").getAsString();
        final String packType = json.get("packType").getAsString().trim();
        this.packType = packType.equalsIgnoreCase("CURSE") ? PackType.CURSE : PackType.CUSTOM;
        this.link = json.get("link").getAsString();
        this.splash = json.get("splash").getAsString();
        this.logo = json.get("logo").getAsString();
        this.gameVersion = json.get("gameVersion").getAsString();
        this.requiredRam = json.get("requiredRam").getAsInt();
        this.recommendedRam = json.get("recommendedRam").getAsInt();
        this.forgeVersion = json.get("forgeVersion").getAsString();
        this.fileSize = this.packType == PackType.CUSTOM ? Optional.of(json.get("fileSize").getAsInt()) : Optional.empty();

        List<OptionalMod> optionalMods = new ArrayList<>();
        for (JsonElement mods : json.get("optionalMods").getAsJsonArray()) {
            optionalMods.add(new OptionalMod(mods.getAsJsonObject()));
        }
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
    public String getSplash() { return splash; }
    public String getLogo() { return logo; }
    public String getGameVersion() { return gameVersion; }
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
    public String getCode() {
        return code;
    }

    //Gets file size of custom pack in megabytes
    public Optional<Integer> getFileSize() {
        return fileSize;
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
    public void setSplash(String splash) {
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
    public void setCode(String code) { this.code = code; }

    public File getInstanceDirectory() {
        return new File(Directories.getInstallDirectory().getPath() + File.separator + "instances" + File.separator + name);
    }

    public boolean isInstalled() {
        for(JsonElement jsonElement : FileUtils.readJsonFromFile(Directories.getDirectoryManifest(Directories.getInstancesDirectory())).getAsJsonArray("packs")) {
            if(jsonElement.getAsJsonObject().get("name").getAsString().equals(getName())) return true;
        }
        return false;
    }

    public boolean isOutdated() {
        for(JsonElement jsonElement : FileUtils.readJsonFromFile(Directories.getDirectoryManifest(Directories.getInstancesDirectory())).getAsJsonArray("packs")) {
            if(jsonElement.getAsJsonObject().get("name").getAsString().equals(getName()) && jsonElement.getAsJsonObject().get("version").getAsString().equals(getVersion())) return false;
        }
        return true;
    }

}
