package net.dirtcraft.dirtlauncher;

import javafx.geometry.Pos;
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

        if (empty || pack == null || pack.getName() == null) {
            setText(null);
        } else {
            setStyle("-fx-background-color: firebrick; -fx-padding: 1px;");
            setText(pack.getName());
            setAlignment(Pos.CENTER);
            setTextAlignment(TextAlignment.CENTER);
            setTextFill(Paint.valueOf("WHITE"));
            setFont(Font.font("System Bold", FontWeight.LIGHT, 20));
        }
        setOnMouseClicked(useless->System.out.println(pack.getName()));
        setTooltip(new Tooltip("Live is short, make most of it..!"));
    }

}