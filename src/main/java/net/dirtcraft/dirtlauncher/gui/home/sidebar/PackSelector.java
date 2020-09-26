package net.dirtcraft.dirtlauncher.gui.home.sidebar;

import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.configuration.Constants;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.gui.components.DiscordPresence;
import net.dirtcraft.dirtlauncher.gui.home.login.LoginBar;
import net.dirtcraft.dirtlauncher.gui.wizards.Install;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public final class PackSelector extends Button implements Comparable<PackSelector> {
    private double lastDragY;
    private final ContextMenu contextMenu;
    private final Modpack modpack;
    private final Region indicator;

    public PackSelector(Modpack modpack) {
        this.modpack = modpack;
        contextMenu = new ContextMenu();
        setGraphic(indicator = getIndicator());
        setCursor(Cursor.HAND);
        setFocusTraversable(false);
        setText(modpack.getName());
        update();

        final Tooltip tooltip = new Tooltip();
        tooltip.setTextAlignment(TextAlignment.LEFT);

        tooltip.setText(String.join("\n", Arrays.asList(
                "ModPack Name: " + modpack.getName(),
                "ModPack Version: " + modpack.getVersion(),
                "Minecraft Version: " + modpack.getGameVersion(),
                "Forge Version: " + modpack.getForgeVersion(),
                "Minimum Ram: " + modpack.getRequiredRam() + " GB",
                "Recommended Ram: " + modpack.getRecommendedRam() + " GB",
                "Direct Connect IP: " + (!modpack.isPixelmon() ? (modpack.getCode() + ".DIRTCRAFT").toUpperCase() : "PIXELMON") + ".GG")
        ));

        Image image;
        try {
            image = new Image(MiscUtils.getResourceStream(
                    Constants.JAR_PACK_IMAGES, modpack.getFormattedName().toLowerCase() + ".png"),
                    128, 128, false, true);
        } catch (Exception exception) {
            System.out.println("Could not find image @ \"" + modpack.getForgeVersion().toLowerCase() + ".png\", requesting from the web...");
            image = new Image(modpack.getLogo(), 128, 128, false, true);
        }

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

    public void update(){
        initContextMenu();
        indicator.pseudoClassStateChanged(PseudoClass.getPseudoClass("installed"), modpack.isInstalled());
        indicator.pseudoClassStateChanged(PseudoClass.getPseudoClass("repair"), modpack.isInstalled() && !modpack.isDependantsInstalled());
        indicator.pseudoClassStateChanged(PseudoClass.getPseudoClass("pinned"), modpack.isInstalled() && modpack.isFavourite());
        indicator.pseudoClassStateChanged(PseudoClass.getPseudoClass("update"), modpack.isInstalled() && modpack.isOutdated());
    }

    public void fire() {
        final LoginBar home = Main.getHome().getLoginBar();
        final Button playButton = home.getActionButton();
        home.getActivePackCell().ifPresent(PackSelector::deactivate);
        home.setActivePackCell(this);
        pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), true);
        DiscordPresence.setDetails("Playing " + modpack.getName());

        if (!MiscUtils.isEmptyOrNull(home.getUsernameField().getText().trim(), home.getPassField().getText().trim()) || Main.getAccounts().hasSelectedAccount()) playButton.setDisable(false);
    }

    private void initContextMenu() {
        contextMenu.getItems().clear();
        if (modpack.isInstalled()) {
            MenuItem reinstall = new MenuItem("Reinstall");
            MenuItem uninstall = new MenuItem("Uninstall");
            MenuItem openFolder = new MenuItem("Open Folder");
            MenuItem favourite = new MenuItem(modpack.isFavourite()? "Unpin" : "Pin");
            contextMenu.getItems().add(favourite);
            contextMenu.getItems().add(reinstall);
            contextMenu.getItems().add(uninstall);
            contextMenu.getItems().add(openFolder);
            contextMenu.pseudoClassStateChanged(PseudoClass.getPseudoClass("installed"), true);

            reinstall.setOnAction(e->{
                uninstall.fire();
                LoginBar loginBar = Main.getHome().getLoginBar();
                Optional<PackSelector> oldPack = loginBar.getActivePackCell();
                loginBar.setActivePackCell(this);
                launchInstallScene();
                getModpack().install();
                oldPack.ifPresent(PackSelector::fire);
                Main.getHome().update();
                initContextMenu();
            });

            uninstall.setOnAction(e->{
                Main.getConfig().getInstanceManifest().remove(this.modpack);
                try {
                    FileUtils.deleteDirectory(modpack.getInstanceDirectory());
                } catch (IOException exception){
                    exception.printStackTrace();
                }
                if (isFavourite()) modpack.toggleFavourite();
                Main.getHome().update();
                initContextMenu();
            });

            openFolder.setOnAction(e->{
                try {
                    Desktop.getDesktop().open(modpack.getInstanceDirectory());
                } catch (IOException exception){
                    exception.printStackTrace();
                }
            });

            favourite.setOnAction(e->{
                modpack.toggleFavourite();
                Main.getHome().updateModpacks();
            });
        } else {
            MenuItem install = new MenuItem("Install");
            contextMenu.getItems().add(install);

            install.setOnAction(e->{
                LoginBar loginBar = Main.getHome().getLoginBar();
                Optional<PackSelector> oldPack = loginBar.getActivePackCell();
                loginBar.setActivePackCell(this);
                launchInstallScene();
                getModpack().install();
                oldPack.ifPresent(PackSelector::fire);
            });
        }
    }

    public Modpack getModpack(){
        return modpack;
    }

    public boolean isFavourite(){
        return modpack.isFavourite();
    }

    public String getName(){
        return modpack.getName();
    }

    @Override
    public int compareTo(@NotNull PackSelector o) {
        if (o.isFavourite() != isFavourite()) return isFavourite()? -1 : 1;
        else if (o.getModpack().isInstalled() != getModpack().isInstalled()) return modpack.isInstalled()? -1 : 1;
        else return getName().compareTo(o.getName());
    }

    public Region getIndicator(){
        Region rectangle = new Region();
        rectangle.getStyleClass().add(Constants.CSS_CLASS_INDICATOR);
        return rectangle;
    }

    public void launchInstallScene() {
        try {
            Stage stage = new Stage();
            stage.setTitle("Installing " + this.getModpack().getName() + "...");
            Parent root = FXMLLoader.load(MiscUtils.getResourceURL(Constants.JAR_SCENES, "install.fxml"));

            stage.initOwner(Main.getHome().getStage());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initStyle(StageStyle.DECORATED);

            stage.getIcons().setAll(MiscUtils.getImage(Constants.JAR_ICONS, "install.png"));

            stage.setScene(new Scene(root, Main.screenDimension.getWidth() / 3, Main.screenDimension.getHeight() / 4));
            stage.setResizable(false);
            stage.setOnCloseRequest(Event::consume);

            stage.show();

            Install.getInstance().ifPresent(install -> {
                TextFlow notificationArea = install.getNotificationText();
                Text notification = new Text("Beginning Download...");
                notification.setFill(Color.WHITE);
                notification.setTextOrigin(VPos.CENTER);
                notification.setTextAlignment(TextAlignment.CENTER);
                notificationArea.getChildren().add(notification);

                notification.setText("Preparing To Install...");
                install.setStage(stage);
            });


        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
