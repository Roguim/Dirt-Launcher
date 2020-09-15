package net.dirtcraft.dirtlauncher.game.installation.tasks.installation.pack;

import javafx.application.Platform;
import net.dirtcraft.dirtlauncher.configuration.Config;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.gui.wizards.Install;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

public class InstallCustomPackTask implements IInstallationTask {

    private final Modpack pack;

    public InstallCustomPackTask(Modpack pack) {
        this.pack = pack;
    }

    public int getNumberSteps() {
        return 2;
    }

    @Override
    public void executeTask(ExecutorService threadService, ProgressContainer progressContainer, Config config) throws IOException {
        // Update Progress
        progressContainer.setProgressText("Downloading Modpack Manifest");
        progressContainer.setNumMinorSteps(2);

        // Prepare Folders
        final File modpackFolder = pack.getInstanceDirectory();
        final File modpackZip = new File(modpackFolder, "modpack.zip");

        FileUtils.deleteDirectory(modpackFolder);
        modpackFolder.mkdirs();

        progressContainer.completeMinorStep();

        // Update Progress
        progressContainer.setProgressText(String.format("Downloading %s Files", pack.getName()));
        progressContainer.setNumMinorSteps(pack.getFileSize().orElse(1));

        // Update UI On Interval Todo make better
        Timer timer = new Timer();
        pack.getFileSize().ifPresent(fileSize -> {
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> Install.getInstance().ifPresent(instance -> instance.getLoadingBar().setProgress(((double) ((modpackZip.length() / 1024 / 1024)) / fileSize))));
                }
            }, 0, 1000);
        });

        // Download the Pack
        FileUtils.copyURLToFile(pack.getLink(), modpackZip);
        timer.cancel();
        progressContainer.setNumMinorSteps(2);
        progressContainer.completeMajorStep();

        // Extract the pack
        progressContainer.setProgressText(String.format("Extracting %s Files", pack.getName()));
        new ZipFile(modpackZip).extractAll(modpackFolder.getPath());
        modpackZip.delete();

        progressContainer.completeMajorStep();
    }

    @Override
    public InstallationStages getRequiredStage() {
        return InstallationStages.INSTALL;
    }
}
