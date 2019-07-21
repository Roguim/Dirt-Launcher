package net.dirtcraft.dirtlauncher.elements;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.css.PseudoClass;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.TextAlignment;
import net.dirtcraft.dirtlauncher.Controllers.Home;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.backend.components.DiscordPresence;
import net.dirtcraft.dirtlauncher.backend.config.Constants;
import net.dirtcraft.dirtlauncher.backend.objects.Listing;
import net.dirtcraft.dirtlauncher.backend.objects.OptionalMod;
import net.dirtcraft.dirtlauncher.backend.utils.FileUtils;
import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Pack  extends Button {
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

    public Pack(JsonObject json) {
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

        getStyleClass().add(Constants.CSS_PACK_CELL);
        contextMenu = new ContextMenu();
        initContextMenu();
        setCursor(Cursor.HAND);
        setFocusTraversable(false);
        setText(name);
        setMinSize(278, 50);
        setPrefSize(278, 50);
        setMaxSize(278, 50);

        final Tooltip tooltip = new Tooltip();
        tooltip.setTextAlignment(TextAlignment.LEFT);
        tooltip.getStyleClass().add(Constants.CSS_PACKLIST);

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
                Constants.PACK_IMAGES, getFormattedName().toLowerCase() + ".png"),
                128, 128, false, true);

        final ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");

        tooltip.setGraphic(imageView);
        tooltip.setGraphicTextGap(50);

        setTooltip(tooltip);
        setOnMouseDragEntered(e-> lastDragY =  e.getY());
        setOnMouseDragged(this::onDrag);
        setOnMouseDragExited(e-> lastDragY = 0);
        setOnContextMenuRequested(e->{
            if (isInstalled()) {
                contextMenu.show(this, e.getScreenX(), e.getScreenY());
            }
        });
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

    public void fire() {
        final LoginBar home = Home.getInstance().getLoginBar();
        final Button playButton = home.getActionButton();
        home.getActivePackCell().ifPresent(Pack::deactivate);
        home.setActivePackCell(this);
        pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), true);
        DiscordPresence.setDetails("Playing " + name);

        if (!MiscUtils.isEmptyOrNull(home.getUsernameField().getText().trim(), home.getPassField().getText().trim())) playButton.setDisable(false);
    }

    private void initContextMenu(){
        MenuItem reinstall = new MenuItem("Reinstall");
        MenuItem uninstall = new MenuItem("Uninstall");
        MenuItem openFolder = new MenuItem("Open Folder");
        reinstall.getStyleClass().add(Constants.CSS_PACK_MENU);
        uninstall.getStyleClass().add(Constants.CSS_PACK_MENU);
        openFolder.getStyleClass().add(Constants.CSS_PACK_MENU);
        reinstall.getStyleClass().add(Constants.CSS_PACK_MENU_OPTION);
        uninstall.getStyleClass().add(Constants.CSS_PACK_MENU_OPTION);
        openFolder.getStyleClass().add(Constants.CSS_PACK_MENU_OPTION);
        contextMenu.getStyleClass().add(Constants.CSS_PACK_MENU);
        contextMenu.setId(Constants.CSS_PACK_MENU);
        contextMenu.getItems().add(reinstall);
        contextMenu.getItems().add(uninstall);
        contextMenu.getItems().add(openFolder);

        reinstall.setOnAction(e->{
            uninstall.fire();
            LoginBar loginBar = Home.getInstance().getLoginBar();
            Optional<Pack> oldPack = loginBar.getActivePackCell();
            loginBar.setActivePackCell(this);
            loginBar.getActionButton().installPack(this);
            oldPack.ifPresent(Pack::fire);
        });

        uninstall.setOnAction(e->{
            JsonObject instanceManifest = FileUtils.readJsonFromFile(Main.getSettings().getDirectoryManifest(Main.getSettings().getInstancesDirectory()));
            if (instanceManifest == null || !instanceManifest.has("packs")) return;
            JsonArray packs = instanceManifest.getAsJsonArray("packs");
            for (int i = 0; i < packs.size(); i++){
                if (Objects.equals(packs.get(i).getAsJsonObject().get("name").getAsString(), name)) packs.remove(i);
            }
            FileUtils.writeJsonToFile(new File(Main.getSettings().getDirectoryManifest(Main.getSettings().getInstancesDirectory()).getPath()), instanceManifest);
            try {
                FileUtils.deleteDirectory(getInstanceDirectory());
            } catch (IOException exception){
                Main.getLogger().error(exception);
            }
        });

        openFolder.setOnAction(e->{
            try {
                Desktop.getDesktop().open(getInstanceDirectory());
            } catch (IOException exception){
                Main.getLogger().error(exception);
            }
        });
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
        return new File(Main.getSettings().getInstancesDirectory().getPath(), getFormattedName());
    }

    public boolean isPixelmon() {
        return this.getCode().equalsIgnoreCase("PIXEL");
    }

    public boolean isInstalled() {
        for(JsonElement jsonElement : FileUtils.readJsonFromFile(Main.getSettings().getDirectoryManifest(Main.getSettings().getInstancesDirectory())).getAsJsonArray("packs")) {
            if(jsonElement.getAsJsonObject().get("name").getAsString().equals(getName())) return true;
        }
        return false;
    }

    public boolean isOutdated() {
        for(JsonElement jsonElement : FileUtils.readJsonFromFile(Main.getSettings().getDirectoryManifest(Main.getSettings().getInstancesDirectory())).getAsJsonArray("packs")) {
            if(jsonElement.getAsJsonObject().get("name").getAsString().equals(getName()) && jsonElement.getAsJsonObject().get("version").getAsString().equals(getVersion())) return false;
        }
        return true;
    }
    public enum PackType {
        CURSE, CUSTOM
    }

}
