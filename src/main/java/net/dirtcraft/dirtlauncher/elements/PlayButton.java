package net.dirtcraft.dirtlauncher.elements;

import javafx.scene.control.Button;

public class PlayButton extends Button {
    private String string;

    public PlayButton(){
        setFocusTraversable(false);
        setId("PlayButton");
    }

    @Override
    public void fire() {
        super.fire();
    }
}
