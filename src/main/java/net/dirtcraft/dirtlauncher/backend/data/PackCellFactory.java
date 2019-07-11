package net.dirtcraft.dirtlauncher.backend.data;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import net.dirtcraft.dirtlauncher.Controllers.Home;
import net.dirtcraft.dirtlauncher.backend.config.CssClasses;
import net.dirtcraft.dirtlauncher.backend.jsonutils.Pack;
import net.dirtcraft.dirtlauncher.backend.utils.Utility;

import java.util.Arrays;

public class PackCellFactory extends ListCell<Pack> {

    public static boolean hasPackSelected = false;

    protected void updateItem(Pack pack, boolean empty) {
        super.updateItem(pack, empty);

        if (empty || pack.getName() == null) {
            getStyleClass().add(CssClasses.PACKLIST);
            setText(null);
        } else {
            getStyleClass().add(CssClasses.PACKLIST);
            String name = pack.getName();

            setText(name);
            setAlignment(Pos.CENTER);
            setTextAlignment(TextAlignment.CENTER);
            setTextFill(Paint.valueOf("WHITE"));
            setFont(Font.font("System Bold", FontWeight.LIGHT, 20));

            setOnMouseClicked(event -> onClick(pack));

            Tooltip tooltip = new Tooltip();
            tooltip.getStyleClass().add(CssClasses.PACKLIST);
            tooltip.setText(String.join("\n", Arrays.asList(
                    "ModPack Name: " + pack.getName(),
                    "ModPack Version: " + pack.getVersion(),
                    "Minecraft Version: " + pack.getGameVersion(),
                    "Forge Version: " + pack.getForgeVersion(),
                    "Minimum Ram: " + pack.getRequiredRam() + "GB",
                    "Recommended Ram: " + pack.getRecommendedRam() + "GB")));
            setTooltip(tooltip);
            setCursor(Cursor.HAND);
        }
    }

    private void onClick(Pack pack) {
        if (!hasPackSelected) hasPackSelected = true;

        Home home = Home.getInstance();
        Button playButton = home.getPlayButton();

        if (!pack.isInstalled()) LoginButtonHandler.setAction(PackAction.INSTALL, pack);
        else if (pack.isOutdated()) LoginButtonHandler.setAction(PackAction.UPDATE, pack);
        else LoginButtonHandler.setAction(PackAction.PLAY, pack);



        if (Utility.isEmptyOrNull(home.getUsernameField().getText().trim(), home.getPasswordField().getText().trim())) return;

        playButton.setDisable(false);
        playButton.setOnAction(e->LoginButtonHandler.onClick());
    }

}