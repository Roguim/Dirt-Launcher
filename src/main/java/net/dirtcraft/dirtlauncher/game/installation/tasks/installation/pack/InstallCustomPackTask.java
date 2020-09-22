package net.dirtcraft.dirtlauncher.game.installation.tasks.installation.pack;

import net.dirtcraft.dirtlauncher.configuration.Config;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.gui.wizards.Install;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.dirtcraft.dirtlauncher.utils.WebUtils;
import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
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
        Optional<Integer> optionalFileSize = pack.getFileSize();

        Timer timer = null;
        if (optionalFileSize.isPresent()) {
            int fileSize = optionalFileSize.get();
            progressContainer.setNumMinorSteps(fileSize);

            // Update UI On Interval Todo make better
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Install.getInstance().ifPresent(instance -> progressContainer.setMinorStepsCompleted((int) ((modpackZip.length() / 1024 / 1024))));
                }
            }, 0, 1000);
        } else progressContainer.setNumMinorSteps(1);

        // Download the Pack
        WebUtils.copyURLToFile(pack.getLink(), modpackZip);
        if (timer != null) timer.cancel();
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
