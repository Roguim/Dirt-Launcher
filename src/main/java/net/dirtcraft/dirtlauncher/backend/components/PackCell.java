package net.dirtcraft.dirtlauncher.backend.components;

import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.TextAlignment;
import net.dirtcraft.dirtlauncher.Controllers.Home;
import net.dirtcraft.dirtlauncher.backend.config.CssClasses;
import net.dirtcraft.dirtlauncher.backend.config.Internal;
import net.dirtcraft.dirtlauncher.backend.objects.Pack;
import net.dirtcraft.dirtlauncher.backend.objects.PackAction;
import net.dirtcraft.dirtlauncher.backend.objects.PackType;
import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;

import java.util.Arrays;

public class PackCell extends Button {

    private Pack pack;
    public PackCell(Pack pack){
        this.pack = pack;
        getStyleClass().add(CssClasses.PACK_CELL);

        setCursor(Cursor.HAND);

        setText(pack.getName());
        setMinSize(278, 50);
        setPrefSize(278, 50);
        setMaxSize(278, 50);
        setOnMouseClicked(event -> onClick(pack));

        Tooltip tooltip = new Tooltip();
        tooltip.setTextAlignment(TextAlignment.LEFT);
        tooltip.getStyleClass().add(CssClasses.PACKLIST);

        tooltip.setText(String.join("\n", Arrays.asList(
                "ModPack Name: " + pack.getName(),
                "ModPack Version: " + pack.getVersion(),
                "Minecraft Version: " + pack.getGameVersion(),
                "Forge Version: " + pack.getForgeVersion(),
                "Minimum Ram: " + pack.getRequiredRam() + " GB",
                "Recommended Ram: " + pack.getRecommendedRam() + " GB",
                "Direct Connect IP: " + (pack.getPackType() != PackType.PIXELMON ? pack.getCode().toUpperCase() + ".DIRTCRAFT" : "PIXELMON") + ".GG")));

        Image image = new Image(MiscUtils.getResourceStream(
                Internal.PACK_IMAGES, pack.getName().trim().toLowerCase().replaceAll("\\s+", "-") + ".png"),
                128, 128, false, true);

        ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");

        tooltip.setGraphic(imageView);
        tooltip.setGraphicTextGap(50);

        setTooltip(tooltip);

        System.out.println("Added the ModPack " + pack.getName() + " to the List View");

    }

    public void deactivate(){
        getStyleClass().remove(CssClasses.PACK_CELL_SELECTED);
    }

    public Pack getPack(){
        return pack;
    }

    private void onClick(Pack pack) {
        PackCell oldCell = Home.getInstance().getActiveCell();
        if (oldCell!= null) oldCell.deactivate();
        getStyleClass().add(CssClasses.PACK_CELL_SELECTED);
        Home.getInstance().setActiveCell(this);
        DiscordPresence.setDetails("Playing " + pack.getName());

        Home home = Home.getInstance();
        Button playButton = home.getPlayButton();

        if (!pack.isInstalled()) LoginButtonHandler.setAction(PackAction.INSTALL, pack);
        else if (pack.isOutdated()) LoginButtonHandler.setAction(PackAction.UPDATE, pack);
        else LoginButtonHandler.setAction(PackAction.PLAY, pack);


        if (MiscUtils.isEmptyOrNull(home.getUsernameField().getText().trim(), home.getPasswordField().getText().trim())) return;

        playButton.setDisable(false);
        playButton.setOnAction(e -> LoginButtonHandler.onClick());
    }
}
