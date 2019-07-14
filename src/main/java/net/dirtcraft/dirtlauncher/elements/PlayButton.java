package net.dirtcraft.dirtlauncher.elements;

import javafx.scene.AccessibleAction;
import javafx.scene.control.Button;

public class PlayButton extends Button {
    private String string;

    public PlayButton(){
        setFocusTraversable(false);
        setId("PlayButton");
    }
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        System.out.println("exec");
        System.out.println(string);
        super.executeAccessibleAction(action, parameters);
    }

    @Override
    public void fire() {
        System.out.println("fyre");
        System.out.println(string);
        super.fire();
    }
}
