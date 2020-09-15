package net.dirtcraft.dirtlauncher.game.installation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.configuration.Config;
import net.dirtcraft.dirtlauncher.exceptions.InvalidManifestException;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.UpdateInstancesManifestTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.installation.AssetsInstallationTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.installation.ForgeInstallationTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.installation.VersionInstallationTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.installation.pack.InstallCursePackTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.installation.pack.InstallCustomPackTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.update.UpdateCursePackTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.update.UpdateCustomPackTask;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.game.modpacks.OptionalMod;
import net.dirtcraft.dirtlauncher.gui.wizards.Install;
import net.dirtcraft.dirtlauncher.configuration.Constants;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;
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

    public void installPack(Modpack pack, List<OptionalMod> optionalMods) throws IOException, InvalidManifestException {
        IInstallationTask packInstallTask;

        // Assigns the proper installation task based on the pack type.
        switch(pack.getPackType()) {
            //case FTB:
            //    packInstallTask = new InstallFTBPackTask(pack);
            //    break;
            case CURSE:
                packInstallTask = new InstallCursePackTask(pack);
                break;
            case CUSTOM:
                packInstallTask = new InstallCustomPackTask(pack);
                break;
            default:
                throw new InvalidManifestException("Invalid Pack Type!");
        }

        installOrUpdatePack(pack, packInstallTask);
    }

    public void updatePack(Modpack pack, List<OptionalMod> optionalMods) throws Exception, InvalidManifestException {
        IInstallationTask packUpdateTask;

        // Assigns the proper update task based on the pack type.
        switch(pack.getPackType()) {
            //case FTB:
            //    packUpdateTask = new UpdateFTBPackTask(pack);
            //    break;
            case CURSE:
                packUpdateTask = new UpdateCursePackTask(pack);
                break;
            case CUSTOM:
                packUpdateTask = new UpdateCustomPackTask(pack);
                break;
            default:
                throw new InvalidManifestException("Invalid Pack Type!");
        }

        try {
            installOrUpdatePack(pack, packUpdateTask);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // Handles the entire installation/update process. The passed task is the appropriate install/update task to be run after the game version tasks are complete.
    private void installOrUpdatePack(Modpack pack, IInstallationTask packInstallTask) throws IOException {
        List<IInstallationTask> installationTasks = new ArrayList();
        // Fetch the game version manifest from Mojang
        JsonObject versionManifest = WebUtils.getVersionManifestJson(pack.getGameVersion());

        // Add tasks for any missing game or forge components
        if(!verifyGameComponentVersion(pack.getGameVersion(), config.getVersionsDirectory(), "versions")) installationTasks.add(new VersionInstallationTask(versionManifest));
        if(!verifyGameComponentVersion(versionManifest.get("assets").getAsString(), config.getAssetsDirectory(), "assets")) installationTasks.add(new AssetsInstallationTask(versionManifest));
        if(!verifyGameComponentVersion(pack.getForgeVersion(), config.getForgeDirectory(), "forgeVersions")) installationTasks.add(new ForgeInstallationTask(pack));

        // Add the pack task as the next task
        installationTasks.add(packInstallTask);

        // Add the manifest update task as the last task.
        installationTasks.add(new UpdateInstancesManifestTask(pack));

        // Tracks the completion of the installation process and updates the progress bar and output text.
        ProgressContainer progressContainer = new ProgressContainer(installationTasks);


        /*
        Async installer. Currently commented out due to the GUI not really supporting it, also could use some cleaning as it looks a bit derp.

        for(InstallationStages stage : InstallationStages.values()){
            System.out.println(stage);
            List<IInstallationTask> tasks = installationTasks.stream().filter(task->task.getRequiredStage() == stage).collect(Collectors.toList());
            List<CompletableFuture<Optional<Exception>>> exceptions = new ArrayList<>();
            tasks.forEach(task->exceptions.add(CompletableFuture.supplyAsync(()->{
                try{
                    System.out.println("L");
                    System.out.println(task.getClass());
                    task.executeTask(downloadManager, progressContainer, config);
                    return Optional.empty();
                } catch (Exception e){
                    return Optional.of(e);
                }
            })));
            for (CompletableFuture<Optional<Exception>> exception : exceptions){
                try {
                    Optional<Exception> optionalException = exception.get();
                    if (optionalException.isPresent()) throw optionalException.get();
                } catch (IOException e){
                    throw e;
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
         */

        // Execute each task in sequence. The subtasks are multithreaded, but the major tasks are sequential intentionally.

        for(IInstallationTask task : installationTasks) {
            task.executeTask(downloadManager, progressContainer, config);
        }

        progressContainer.completeMajorStep();

        /*
            --- Installation Completed ---
         */


        // Update the UI and allow the user to launch the pack/
        Platform.runLater(() ->
                Install.getInstance().ifPresent(install -> {
                    ((Text)install.getNotificationText().getChildren().get(0)).setText("Successfully Installed " + pack.getName() + "!");
                    install.getButtonPane().setVisible(true);
                    Stage installStage = install.getStageUnsafe();
                    if (installStage != null) installStage.setOnCloseRequest(event -> {
                        if (!install.getButtonPane().isVisible()) event.consume();
                    });
                    Main.getHome().update();
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
        return JsonUtils.readJsonFromFile(config.getDirectoryManifest(folder));
    }

}
