package net.dirtcraft.dirtlauncher.backend.utils;

import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import net.dirtcraft.dirtlauncher.Controllers.Home;

public class TooltipUtils {

    public static void bindTooltip(Tooltip tooltip) {
        Node node = Home.getInstance().getPackList();

    }

}
