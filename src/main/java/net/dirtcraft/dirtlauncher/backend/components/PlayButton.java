package net.dirtcraft.dirtlauncher.backend.components;

import javafx.scene.AccessibleAction;
import javafx.scene.control.Button;

public class PlayButton extends Button {
    private String string;
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        System.out.println("FFFFFFP");
        System.out.println(string);
        super.executeAccessibleAction(action, parameters);
    }

    @Override
    public void fire() {
        System.out.println("FFFFFFP");
        System.out.println(string);
        super.fire();
    }
}
