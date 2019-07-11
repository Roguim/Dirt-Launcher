package net.dirtcraft.dirtlauncher.backend.installation;

import javafx.scene.text.Text;
import net.dirtcraft.dirtlauncher.Controllers.Install;
import net.dirtcraft.dirtlauncher.backend.jsonutils.OptionalMod;
import net.dirtcraft.dirtlauncher.backend.jsonutils.Pack;

import java.util.List;

public class DownloadManager {

    public static void completePackSetup(Pack pack, List<OptionalMod> optionalMods) {
        boolean installMinecraft = true;
        boolean installAssets = true;
        boolean installLibraries = true;
        boolean installForge = true;
        boolean installPack = true;
    }

    public static void setProgressText(String text) {
        ((Text) Install.getInstance().getNotificationText().getChildren().get(0)).setText(text);
    }

    public static void setProgressPercent(int completed, int total) {
        Install.getInstance().getLoadingBar().setProgress(Math.ceil(completed / total));
    }

    public static void setTotalProgressPercent(int completed, int total) {
        Install.getInstance().getBottomBar().setProgress(Math.ceil(completed / total));
    }

}
