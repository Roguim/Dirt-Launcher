package net.dirtcraft.dirtlauncher.gui.home.sidebar;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.css.PseudoClass;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.TextAlignment;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.game.objects.Listing;
import net.dirtcraft.dirtlauncher.game.objects.OptionalMod;
import net.dirtcraft.dirtlauncher.gui.components.DiscordPresence;
import net.dirtcraft.dirtlauncher.gui.home.login.LoginBar;
import net.dirtcraft.dirtlauncher.utils.Constants;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

public final class Pack extends Button {
    private double lastDragY;
    private String version;
    private final ContextMenu contextMenu;
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

    Pack(JsonObject json) {
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

        contextMenu = new ContextMenu();
        initContextMenu();
        setCursor(Cursor.HAND);
        setFocusTraversable(false);
        setText(name);

        final Tooltip tooltip = new Tooltip();
        tooltip.setTextAlignment(TextAlignment.LEFT);

        tooltip.setText(String.join("\n", Arrays.asList(
                "ModPack Name: " + name,
                "ModPack Version: " + version,
                "Minecraft Version: " + gameVersion,
                "Forge Version: " + forgeVersion,
                "Minimum Ram: " + requiredRam + " GB",
                "Recommended Ram: " + recommendedRam + " GB",
                "Direct Connect IP: " + (!isPixelmon() ? (code + ".DIRTCRAFT").toUpperCase() : "PIXELMON") + ".GG")
        ));

        final Image image = new Image(MiscUtils.getResourceStream(
                Constants.JAR_PACK_IMAGES, getFormattedName().toLowerCase() + ".png"),
                128, 128, false, true);

        final ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");

        tooltip.setGraphic(imageView);
        tooltip.setGraphicTextGap(50);

        setTooltip(tooltip);
        setOnMouseDragEntered(e-> lastDragY =  e.getY());
        setOnMouseDragged(this::onDrag);
        setOnMouseDragExited(e-> lastDragY = 0);
        setOnContextMenuRequested(e->contextMenu.show(this, e.getScreenX(), e.getScreenY()));
    }

    private void deactivate(){
        pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), false);
    }

    private void onDrag(MouseEvent event) {
        if (event.isPrimaryButtonDown()) {
            ScrollPane window = (ScrollPane) this.getParent().getParent().getParent().getParent();
            double change = (lastDragY - event.getY()) / window.getHeight();
            window.setVvalue(window.getVvalue() + change);
            lastDragY = change;
        }
    }

    public void updateInstallStatus(){
        initContextMenu();
    }

    public void fire() {
        final LoginBar home = Main.getHome().getLoginBar();
        final Button playButton = home.getActionButton();
        home.getActivePackCell().ifPresent(Pack::deactivate);
        home.setActivePackCell(this);
        pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), true);
        DiscordPresence.setDetails("Playing " + name);

        if (!MiscUtils.isEmptyOrNull(home.getUsernameField().getText().trim(), home.getPassField().getText().trim()) || Main.getAccounts().hasSelectedAccount()) playButton.setDisable(false);
    }

    private void initContextMenu(){
        contextMenu.getItems().clear();
        if (isInstalled()) {
            MenuItem reinstall = new MenuItem("Reinstall");
            MenuItem uninstall = new MenuItem("Uninstall");
            MenuItem openFolder = new MenuItem("Open Folder");
            contextMenu.getItems().add(reinstall);
            contextMenu.getItems().add(uninstall);
            contextMenu.getItems().add(openFolder);
            contextMenu.pseudoClassStateChanged(PseudoClass.getPseudoClass("installed"), true);

            reinstall.setOnAction(e->{
                uninstall.fire();
                LoginBar loginBar = Main.getHome().getLoginBar();
                Optional<Pack> oldPack = loginBar.getActivePackCell();
                loginBar.setActivePackCell(this);
                loginBar.getActionButton().installPack(this);
                oldPack.ifPresent(Pack::fire);
            });

            uninstall.setOnAction(e->{
                JsonObject instanceManifest = FileUtils.readJsonFromFile(Main.getConfig().getDirectoryManifest(Main.getConfig().getInstancesDirectory()));
                if (instanceManifest == null || !instanceManifest.has("packs")) return;
                JsonArray packs = instanceManifest.getAsJsonArray("packs");
                for (int i = 0; i < packs.size(); i++){
                    if (Objects.equals(packs.get(i).getAsJsonObject().get("name").getAsString(), name)) packs.remove(i);
                }
                FileUtils.writeJsonToFile(new File(Main.getConfig().getDirectoryManifest(Main.getConfig().getInstancesDirectory()).getPath()), instanceManifest);
                try {
                    FileUtils.deleteDirectory(getInstanceDirectory());
                } catch (IOException exception){
                    exception.printStackTrace();
                }
            });

            openFolder.setOnAction(e->{
                try {
                    Desktop.getDesktop().open(getInstanceDirectory());
                } catch (IOException exception){
                    exception.printStackTrace();
                }
            });
        } else {
            MenuItem install = new MenuItem("Install");
            contextMenu.getItems().add(install);

            install.setOnAction(e->{
                LoginBar loginBar = Main.getHome().getLoginBar();
                Optional<Pack> oldPack = loginBar.getActivePackCell();
                loginBar.setActivePackCell(this);
                loginBar.getActionButton().installPack(this);
                oldPack.ifPresent(Pack::fire);
            });
        }
    }

    public String getName() {
        return name;
    }
    public String getFormattedName() { return name.replaceAll("\\s+", "-"); }
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
        if (fileSize == null) return Optional.empty();
        else return Optional.of(fileSize);
    }

    public Optional<List<Listing>> getListings() {
        if (listings == null) return Optional.empty();
        else return Optional.of(listings);
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public File getInstanceDirectory() {
        return new File(Main.getConfig().getInstancesDirectory().getPath(), getFormattedName());
    }

    public boolean isPixelmon() {
        return this.getCode().equalsIgnoreCase("PIXEL");
    }

    public boolean isInstalled() {
        for(JsonElement jsonElement : FileUtils.readJsonFromFile(Main.getConfig().getDirectoryManifest(Main.getConfig().getInstancesDirectory())).getAsJsonArray("packs")) {
            if(jsonElement.getAsJsonObject().get("name").getAsString().equals(getName())) return true;
        }
        return false;
    }

    public boolean isOutdated() {
        for(JsonElement jsonElement : FileUtils.readJsonFromFile(Main.getConfig().getDirectoryManifest(Main.getConfig().getInstancesDirectory())).getAsJsonArray("packs")) {
            if(jsonElement.getAsJsonObject().get("name").getAsString().equals(getName()) && jsonElement.getAsJsonObject().get("version").getAsString().equals(getVersion())) return false;
        }
        return true;
    }
    public enum PackType {
        CURSE, CUSTOM
    }

}
