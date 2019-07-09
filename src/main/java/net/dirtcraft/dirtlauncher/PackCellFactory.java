package net.dirtcraft.dirtlauncher;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import net.dirtcraft.dirtlauncher.backend.JsonUtils.Pack;

public class PackCellFactory extends ListCell<Pack> {

    public PackCellFactory() {    }

    protected void updateItem(Pack pack, boolean empty) {
        super.updateItem(pack, empty);

        if (empty || pack.getName() == null) {
            getStyleClass().add("packlist");
            setText(null);
        } else {
            getStyleClass().add("packlist");
            String name = pack.getName();
            String version = pack.getVersion();
            setText(name);
            setAlignment(Pos.CENTER);
            setTextAlignment(TextAlignment.CENTER);
            setTextFill(Paint.valueOf("WHITE"));
            setFont(Font.font("System Bold", FontWeight.LIGHT, 20));

            Label label = new Label(version);
            label.
            setGraphic(label);

            setOnMouseClicked(useless->System.out.println(name));

            setTooltip(new Tooltip(version));
        }
    }

}