package net.dirtcraft.dirtlauncher.game.modpacks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.configuration.manifests.VersionManifest;
import net.dirtcraft.dirtlauncher.data.Curse.CurseModpackManifest;
import net.dirtcraft.dirtlauncher.data.Minecraft.JavaVersion;
import net.dirtcraft.dirtlauncher.exceptions.InvalidManifestException;
import net.dirtcraft.dirtlauncher.game.LaunchGame;
import net.dirtcraft.dirtlauncher.game.authentification.Account;
import net.dirtcraft.dirtlauncher.game.installation.InstallationManager;
import net.dirtcraft.dirtlauncher.game.serverlist.Listing;
import net.dirtcraft.dirtlauncher.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static net.dirtcraft.dirtlauncher.utils.JsonUtils.getJsonElement;
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
    private final ModLoader modLoader;
    private final String modloaderVersion;
    private final List<OptionalMod> optionalMods;
    private final List<Listing> listings;
    private final UpdateTracker updateTracker;
    boolean favourite = false;

    public Modpack(CurseModpackManifest modpackManifest, String link){
        this.version = modpackManifest.version;
        this.name = modpackManifest.name;
        this.code = null;
        this.packType = PackType.CURSE;
        this.link = link;
        this.splash = null;
        this.logo = null;
        this.gameVersion = modpackManifest.minecraft.version;
        this.requiredRam = -1;
        this.recommendedRam = -1;
        this.modLoader = ModLoader.FORGE;
        this.modloaderVersion = modpackManifest.minecraft.modLoaders.stream().findFirst().map(ml->ml.id).orElse("N/A");
        this.optionalMods = new ArrayList<>();
        this.listings = new ArrayList<>();
        this.updateTracker = UpdateTracker.CURSE;
    }

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
        final Optional<String> packType = getJsonElement(json, JsonElement::getAsString, "packType");
        final Optional<String> updateTracker = getJsonElement(json, JsonElement::getAsString, "updateTracker");
        final Optional<String> modLoader = getJsonElement(json, JsonElement::getAsString, "modLoader");
        this.name = json.get("name").getAsString().trim();
        this.version = json.get("version").getAsString();
        this.code = json.get("code").getAsString();
        this.packType = packType.map(PackType::valueOf).orElse(PackType.CUSTOM);
        this.updateTracker = updateTracker.map(UpdateTracker::valueOf).orElse(UpdateTracker.DIRTCRAFT);
        this.modLoader = modLoader.map(ModLoader::valueOf).orElse(ModLoader.FORGE);
        this.link = json.get("link").getAsString();
        this.splash = json.get("splash").getAsString();
        this.logo = json.get("logo").getAsString();
        this.gameVersion = json.get("gameVersion").getAsString();
        this.requiredRam = json.get("requiredRam").getAsInt();
        this.recommendedRam = json.get("recommendedRam").getAsInt();
        this.modloaderVersion = json.get("forgeVersion").getAsString();
        this.optionalMods = optionalMods;
    }

    public Optional<List<Listing>> getListings() {
        return Optional.ofNullable(listings);
    }

    public File getInstanceDirectory() {
        return Main.getConfig().getInstanceManifest().getDirectory().resolve(getFormattedName()).toFile();
    }

    public boolean isPixelmon() {
        return this.getCode().equalsIgnoreCase("PIXEL");
    }

    public CompletableFuture<Void> install(){
        Logger.INSTANCE.debug("Installing the pack");
        return CompletableFuture.runAsync(() -> {
            try {
                InstallationManager.getInstance().installPack(this, Collections.emptyList());
            } catch (IOException | InvalidManifestException e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<Void> update(){
        Logger.INSTANCE.debug("Updated the game");
        return CompletableFuture.runAsync(() -> {
            try {
                InstallationManager.getInstance().updatePack(this, Collections.emptyList());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void launch(){
        try {
            Main.getAccounts().verifySelected();
            Account session = Main.getAccounts().getSelectedAccountUnchecked();
            LaunchGame.isGameRunning = true;
            LaunchGame.loadServerList(this);
            LaunchGame.launchPack(this, session);
        } catch (Exception e){
            Logger.INSTANCE.error(e);
        }
    }

    public boolean isOutdated() {
        return Main.getConfig().getInstanceManifest().stream()
                .noneMatch(pack->pack.version.equals(getVersion()));
    }

    public boolean isInstalled() {
        return Main.getConfig().getInstanceManifest().stream()
                .anyMatch(pack->pack.name.equals(getName()))
                && getInstanceDirectory().exists();
    }

    public boolean isDependantsInstalled(){
        VersionManifest versionManifest = Main.getConfig().getVersionManifest();
        return versionManifest.isInstalled(gameVersion)
                && Main.getConfig().getAssetManifest().isInstalled(gameVersion)
                && isModloaderInstalled()
                && versionManifest.get(gameVersion)
                .map(VersionManifest.Entry::getJavaVersion)
                .orElse(JavaVersion.LEGACY)
                .isInstalled(Main.getConfig().getJavaDirectory());
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
        return modloaderVersion;
    }

    public List<OptionalMod> getOptionalMods() {
        return optionalMods;
    }

    public String getCode() {
        return code;
    }

    private boolean isModloaderInstalled(){
        switch (modLoader){
            case FORGE: return Main.getConfig().getForgeManifest().isInstalled(modloaderVersion);
            default: return true;
        }
    }

    public enum PackType {
        CURSE, FTB, CUSTOM
    }

    public enum UpdateTracker{
        CURSE, DIRTCRAFT, NONE
    }

    public enum ModLoader{
        FORGE, NONE
    }
}
