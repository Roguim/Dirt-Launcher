package net.dirtcraft.dirtlauncher;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import net.dirtcraft.dirtlauncher.backend.JsonUtils.Pack;

import java.util.Arrays;

public class PackCellFactory extends ListCell<Pack> {

    protected void updateItem(Pack pack, boolean empty) {
        super.updateItem(pack, empty);

        if (empty || pack.getName() == null) {
            getStyleClass().add("packlist");
            setText(null);
        } else {
            getStyleClass().add("packlist");
            String name = pack.getName();

            setText(name);
            setAlignment(Pos.CENTER);
            setTextAlignment(TextAlignment.CENTER);
            setTextFill(Paint.valueOf("WHITE"));
            setFont(Font.font("System Bold", FontWeight.LIGHT, 20));

            setOnMouseClicked(useless -> System.out.println(name));

            Tooltip tooltip = new Tooltip();
            tooltip.getStyleClass().add("packlist");
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

}