package sample.Events;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import sample.Main;

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
