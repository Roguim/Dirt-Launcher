package net.dirtcraft.dirtlauncher.Events;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import net.dirtcraft.dirtlauncher.Main;

public class ButtonClick implements EventHandler<ActionEvent> {

    private final Main main;

    public ButtonClick(Main main) {
        this.main = main;
    }

    @Override
    public void handle(ActionEvent event) {
        if (event.getSource() == main.button) {

        }
    }
}
