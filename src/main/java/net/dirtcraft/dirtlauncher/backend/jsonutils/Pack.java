package net.dirtcraft.dirtlauncher.backend.jsonutils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.backend.config.Paths;
import net.dirtcraft.dirtlauncher.backend.objects.PackType;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

    public Pack(JsonObject json) {
        this.name = json.get("name").getAsString();
        this.version = json.get("version").getAsString();
        this.code = json.get("code").getAsString();
        final String packType = json.get("packType").getAsString().trim();
        this.packType =  packType.equalsIgnoreCase("CURSE") ? PackType.CURSE : (packType.equalsIgnoreCase("PIXELMON") ? PackType.PIXELMON : PackType.CUSTOM);
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
        return new File(Paths.getInstallDirectory().getPath() + File.separator + "instances" + File.separator + name);
    }

    public boolean isInstalled() {
        return getInstanceDirectory().exists();
    }

    public boolean isOutdated() {
        return false;
    }

    public void installPack() throws IOException {
        // Create the instance folder
        getInstanceDirectory().mkdirs();
        // Check if the version is downloaded, and if not download it.
        if(!isGameVersionDownloaded()) downloadGameVersion();
    }

    private File getGameVersionFolder() {
        return new File(Paths.getInstallDirectory().getPath() + File.separator + "versions" + File.separator + gameVersion);
    }

    private boolean isGameVersionDownloaded() {
        return getGameVersionFolder().exists();
    }

    private void downloadGameVersion() throws IOException {
        // Prepare the version folder
        getGameVersionFolder().mkdirs();
        // Fetch the version JSON
        JsonObject versionManifest = JsonFetcher.getVersionManifestJson(gameVersion);
        // Save it to a file
        FileWriter manifestFile = new FileWriter(getGameVersionFolder().getPath() + File.separator + gameVersion + ".json");
        manifestFile.write(versionManifest.toString());
        manifestFile.close();
        // Download the game jar
        FileUtils.copyURLToFile(new URL(versionManifest.getAsJsonObject("downloads").getAsJsonObject("client").get("url").getAsString()), new File(getGameVersionFolder().getPath() + File.separator + gameVersion + ".jar"));
        // Create assets folder
        File assetsFolder = new File(getGameVersionFolder().getPath() + File.separator + "assets");
        assetsFolder.mkdirs();
        // Fetch the assets JSON
        System.out.println("1");
        JsonObject assetsManifest = JsonFetcher.getJsonFromUrl(versionManifest.getAsJsonObject("assetIndex").get("url").getAsString());
        // Save it to a file
        System.out.println("2");
        FileWriter assetsManifestFile = new FileWriter(assetsFolder.getPath() + File.separator + "indexes" + File.separator + versionManifest.getAsJsonObject("assetIndex").get("id").getAsString() + ".json");
        assetsManifestFile.write(assetsManifest.toString());
        assetsManifestFile.close();
        // Fetch individual assets
        System.out.println("3");
        assetsManifest.getAsJsonObject("objects").keySet().parallelStream().forEach(assetKey -> {
            try {
                System.out.println("4");
                String hash = assetsManifest.getAsJsonObject("objects").getAsJsonObject("assetKey").get("hash").getAsString();
                String outputFilePath = assetsFolder.getPath() + File.separator + "objects" + File.separator + hash.substring(0, 2);
                new File(outputFilePath).mkdirs();
                FileUtils.copyURLToFile(new URL("http://resources.download.minecraft.net/" + hash.substring(0, 2) + "/" + hash), new File(outputFilePath));
                System.out.println("5");
            } catch (IOException e) { e.printStackTrace(); }
        });
        System.out.println("6");
    }
}
