package net.dirtcraft.dirtlauncher.game.installation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import net.dirtcraft.dirtlauncher.Data.Config;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.game.installation.exceptions.InvalidManifestException;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.UpdateInstancesManifestTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.installation.*;
import net.dirtcraft.dirtlauncher.game.installation.tasks.installation.pack.InstallCursePackTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.installation.pack.InstallCustomPackTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.installation.pack.InstallFTBPackTask;
import net.dirtcraft.dirtlauncher.game.objects.OptionalMod;
import net.dirtcraft.dirtlauncher.gui.home.login.ActionButton;
import net.dirtcraft.dirtlauncher.gui.home.sidebar.Pack;
import net.dirtcraft.dirtlauncher.gui.wizards.Install;
import net.dirtcraft.dirtlauncher.utils.Constants;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.dirtcraft.dirtlauncher.utils.WebUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.StreamSupport;

public class InstallationManager {

    private final ExecutorService downloadManager;
    private final Config config;

    private InstallationManager() {
        downloadManager = Executors.newFixedThreadPool(Constants.MAX_DOWNLOAD_THREADS);
        config = Main.getConfig();
    }

    // Lazy Loaded Singleton Container
    private static class InstallationManagerLoader {
        static final InstallationManager INSTANCE = new InstallationManager();
    }

    // Singleton Access
    public static InstallationManager getInstance() {
        return InstallationManagerLoader.INSTANCE;
    }

    public void installPack(Pack pack, List<OptionalMod> optionalMods) throws IOException, InvalidManifestException {
        System.out.println("-1");
        IInstallationTask packInstallTask;

        System.out.println("A");
        // Assigns the proper installation task based on the pack type.
        switch(pack.getPackType()) {
            case CURSE:
                System.out.println("B");
                packInstallTask = new InstallCursePackTask(pack);
                System.out.println("C");
                break;
            case FTB:
                packInstallTask = new InstallFTBPackTask(pack);
                break;
            case CUSTOM:
                packInstallTask = new InstallCustomPackTask(pack);
                break;
            default:
                System.out.println("D");
                throw new InvalidManifestException("Invalid Pack Type!");
        }

        System.out.println("E");
        installOrUpdatePack(pack, packInstallTask);
    }

    public void updatePack(Pack pack, List<OptionalMod> optionalMods) throws Exception, InvalidManifestException {
        //IInstallationTask packUpdateTask;

        // Assigns the proper update task based on the pack type.
        switch(pack.getPackType()) {
            case CURSE:
                //packUpdateTask = new UpdateCursePackTask(pack);
                break;
            case FTB:
                //packUpdateTask = new UpdateFTBPackTask(pack);
                break;
            case CUSTOM:
                //packUpdateTask = new UpdateCustomPackTask(pack);
                break;
            default:
                throw new InvalidManifestException("Invalid Pack Type!");
        }

        //installOrUpdatePack(pack, packUpdateTask);
    }

    // Handles the entire installation/update process. The passed task is the appropriate install/update task to be run after the game version tasks are complete.
    private void installOrUpdatePack(Pack pack, IInstallationTask packInstallTask) throws IOException {
        System.out.println("F");
        List<IInstallationTask> installationTasks = new ArrayList();
        // Fetch the game version manifest from Mojang
        JsonObject versionManifest = WebUtils.getVersionManifestJson(pack.getGameVersion());

        System.out.println("G");
        // Add tasks for any missing game or forge components
        if(!verifyGameComponentVersion(pack.getGameVersion(), config.getVersionsDirectory(), "versions")) installationTasks.add(new VersionInstallationTask(versionManifest));
        System.out.println("Uno");
        if(!verifyGameComponentVersion(versionManifest.get("assets").getAsString(), config.getAssetsDirectory(), "assets")) installationTasks.add(new AssetsInstallationTask(versionManifest));
        System.out.println("Dos");
        if(!verifyGameComponentVersion(pack.getForgeVersion(), config.getForgeDirectory(), "forgeVersions")) installationTasks.add(new ForgeInstallationTask(pack));

        System.out.println("H");
        // Add the pack task as the next task
        installationTasks.add(packInstallTask);

        System.out.println("I");
        // Add the manifest update task as the last task.
        installationTasks.add(new UpdateInstancesManifestTask(pack));

        System.out.println("J");
        // Tracks the completion of the installation process and updates the progress bar and output text.
        ProgressContainer progressContainer = new ProgressContainer(installationTasks);

        System.out.println("K");
        // Execute each task in sequence. The subtasks are multithreaded, but the major tasks are sequential intentionally.
        for(IInstallationTask task : installationTasks) {
            System.out.println("L");
            System.out.println(task.getClass());
            task.executeTask(downloadManager, progressContainer, config);
        }

        /*
            --- Installation Completed ---
         */

        System.out.println("M");
        pack.updateInstallStatus();

        // Update the UI and allow the user to launch the pack/
        Platform.runLater(() ->
                Install.getInstance().ifPresent(install -> {
                    ((Text)install.getNotificationText().getChildren().get(0)).setText("Successfully Installed " + pack.getName() + "!");
                    install.getButtonPane().setVisible(true);
                    Stage installStage = install.getStageUnsafe();
                    if (installStage != null) installStage.setOnCloseRequest(event -> {
                        if (!install.getButtonPane().isVisible()) event.consume();
                    });
                    Main.getHome().getLoginBar().updatePlayButton(ActionButton.Types.PLAY);
                }));
    }

    // Checks whether or not a base game component (version, assets, and libraries) is already installed.
    private boolean verifyGameComponentVersion(String desiredVersion, File folder, String key) {
        return StreamSupport.stream(getJsonFromManifest(folder).getAsJsonArray(key).spliterator(), false)
                .map(JsonElement::getAsJsonObject)
                .map(obj -> obj.get("version"))
                .map(JsonElement::getAsString)
                .anyMatch(version -> version.equals(desiredVersion));
    }

    private JsonObject getJsonFromManifest(File folder) {
        return FileUtils.readJsonFromFile(config.getDirectoryManifest(folder));
    }

}
