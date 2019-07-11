package net.dirtcraft.dirtlauncher.backend.components;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import net.dirtcraft.dirtlauncher.Controllers.Home;
import net.dirtcraft.dirtlauncher.backend.config.CssClasses;
import net.dirtcraft.dirtlauncher.backend.config.Internal;
import net.dirtcraft.dirtlauncher.backend.jsonutils.Pack;
import net.dirtcraft.dirtlauncher.backend.objects.PackAction;
import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;

import java.util.Arrays;

public class PackCellFactory extends ListCell<Pack> {

    public static boolean hasPackSelected = false;

    protected void updateItem(Pack pack, boolean empty) {
        super.updateItem(pack, empty);

        if (empty || pack.getName() == null) {
            getStyleClass().add(CssClasses.PACKLIST);
            setText(null);
        } else {

            setCursor(Cursor.HAND);

            getStyleClass().add(CssClasses.PACKLIST);

            setText(pack.getName());
            setAlignment(Pos.CENTER);
            setTextAlignment(TextAlignment.CENTER);
            setTextFill(Paint.valueOf("WHITE"));
            setFont(Font.font("System Bold", FontWeight.LIGHT, 20));

            setOnMouseClicked(event -> onClick(pack));

            Tooltip tooltip = new Tooltip();
            tooltip.setTextAlignment(TextAlignment.LEFT);
            tooltip.getStyleClass().add(CssClasses.PACKLIST);

            tooltip.setText(String.join("\n", Arrays.asList(
                    "ModPack Name: " + pack.getName(),
                    "ModPack Version: " + pack.getVersion(),
                    "Minecraft Version: " + pack.getGameVersion(),
                    "Forge Version: " + pack.getForgeVersion(),
                    "Minimum Ram: " + pack.getRequiredRam() + "GB",
                    "Recommended Ram: " + pack.getRecommendedRam() + "GB")));

            Image image = new Image(MiscUtils.getResourceStream(
                    Internal.PACK_IMAGES, pack.getName().trim().toLowerCase().replaceAll("\\s+","") + ".png"),
                    128, 128, false, true);
            ImageView imageView = new ImageView(image);

            tooltip.setGraphic(imageView);
            tooltip.setGraphicTextGap(50);

            setTooltip(tooltip);

        }
    }

    private void onClick(Pack pack) {
        if (!hasPackSelected) hasPackSelected = true;

        Home home = Home.getInstance();
        Button playButton = home.getPlayButton();

        if (!pack.isInstalled()) LoginButtonHandler.setAction(PackAction.INSTALL, pack);
        else if (pack.isOutdated()) LoginButtonHandler.setAction(PackAction.UPDATE, pack);
        else LoginButtonHandler.setAction(PackAction.PLAY, pack);



        if (MiscUtils.isEmptyOrNull(home.getUsernameField().getText().trim(), home.getPasswordField().getText().trim())) return;

        playButton.setDisable(false);
        playButton.setOnAction(e->LoginButtonHandler.onClick());
    }

}