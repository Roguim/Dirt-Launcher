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
import net.dirtcraft.dirtlauncher.Controller;
import net.dirtcraft.dirtlauncher.backend.Config.CssClasses;
import net.dirtcraft.dirtlauncher.backend.JsonUtils.Pack;
import net.dirtcraft.dirtlauncher.backend.Utils.Utility;

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

            setOnMouseClicked(event -> onClick());

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

    private void onClick() {
        if (!hasPackSelected) hasPackSelected = true;

        Controller controller = Controller.getInstance();
        if (Utility.isEmptyOrNull(controller.getUsernameField().getText().trim(), controller.getPasswordField().getText().trim())) return;

        Button playButton = controller.getPlayButton();
        playButton.setDisable(false);
        playButton.setOnAction(action -> LoginButtonHandler.onClick());
    }

}