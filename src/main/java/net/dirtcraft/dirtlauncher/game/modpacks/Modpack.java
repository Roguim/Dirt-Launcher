package net.dirtcraft.dirtlauncher.game.modpacks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.game.LaunchGame;
import net.dirtcraft.dirtlauncher.game.authentification.Account;
import net.dirtcraft.dirtlauncher.game.installation.InstallationManager;
import net.dirtcraft.dirtlauncher.game.installation.exceptions.InvalidManifestException;
import net.dirtcraft.dirtlauncher.utils.Manifests;
import net.dirtcraft.dirtlauncher.game.serverlist.Listing;
import net.dirtcraft.dirtlauncher.utils.Constants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Modpack {
    private final String version;
    private final String name;
    private final String code;
    private final PackType packType;
    private final String link;
    private final String splash;
    private final String logo;
    private final String gameVersion;
    private final int requiredRam;
    private final int recommendedRam;
    private final String forgeVersion;
    private final List<OptionalMod> optionalMods;
    private final Integer fileSize;
    private final List<Listing> listings;
    boolean favourite = false;

    Modpack(JsonObject json) {
        final List<OptionalMod> optionalMods = new ArrayList<>();
        for (JsonElement mods : json.get("optionalMods").getAsJsonArray()) {
            optionalMods.add(new OptionalMod(mods.getAsJsonObject()));
        }
        if (!json.has("serverList")) this.listings = null;
        else {
            final List<Listing> listings = new ArrayList<>();
            for (JsonElement servers : json.get("serverList").getAsJsonArray()) {
                listings.add(new Listing(servers.getAsJsonObject()));
            }
            this.listings = listings;
        }
        final String packType = json.get("packType").getAsString().trim();
        this.name = json.get("name").getAsString().trim();
        this.version = json.get("version").getAsString();
        this.code = json.get("code").getAsString();
        this.packType = packType.equalsIgnoreCase("CURSE") ? PackType.CURSE : PackType.CUSTOM;
        this.link = json.get("link").getAsString();
        this.splash = json.get("splash").getAsString();
        this.logo = json.get("logo").getAsString();
        this.gameVersion = json.get("gameVersion").getAsString();
        this.requiredRam = json.get("requiredRam").getAsInt();
        this.recommendedRam = json.get("recommendedRam").getAsInt();
        this.forgeVersion = json.get("forgeVersion").getAsString();
        this.fileSize = (this.packType == PackType.CUSTOM) ? json.get("fileSize").getAsInt() : null;
        this.optionalMods = optionalMods;
    }

    //Gets file size of custom pack in megabytes
    public Optional<Integer> getFileSize() {
        if (fileSize == null) return Optional.empty();
        else return Optional.of(fileSize);
    }

    public Optional<List<Listing>> getListings() {
        if (listings == null) return Optional.empty();
        else return Optional.of(listings);
    }

    public File getInstanceDirectory() {
        return new File(Main.getConfig().getInstancesDirectory().getPath(), getFormattedName());
    }

    public boolean isPixelmon() {
        return this.getCode().equalsIgnoreCase("PIXEL");
    }

    public CompletableFuture<Void> install(){
        if (Constants.DEBUG) {
            System.out.println("Installing the pack");
        }
        return CompletableFuture.runAsync(() -> {
            try {
                //DownloadManager.completePackSetup(modPack, Collections.emptyList(), false);
                InstallationManager.getInstance().installPack(this, Collections.emptyList());
            } catch (IOException | InvalidManifestException e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<Void> update(){
        if (Constants.DEBUG) {
            System.out.println("Updated the game");
        }
        return CompletableFuture.runAsync(() -> {
            try {
                InstallationManager.getInstance().updatePack(this, Collections.emptyList());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void launch(){
        Account session = Main.getAccounts().getSelectedAccountUnchecked();
        LaunchGame.isGameRunning = true;
        LaunchGame.loadServerList(this);
        LaunchGame.launchPack(this, session);
    }

    public boolean isInstalled() {
        return Manifests.INSTANCE.stream()
                .anyMatch(pack->pack.name.equals(getName()));
    }

    public boolean isOutdated() {
        return Manifests.INSTANCE.stream()
                .noneMatch(pack->pack.version.equals(getVersion()));
    }

    public boolean isFavourite(){
        return favourite;
    }

    public void toggleFavourite(){
        this.favourite = !this.favourite;
        ModpackManager.getInstance().saveAsync();
    }

    public String getName() {
        return name;
    }

    public String getFormattedName() {
        return name.replaceAll("\\s+", "-");
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

    public String getCode() {
        return code;
    }

    public enum PackType {
        CURSE, FTB, CUSTOM
    }
}
