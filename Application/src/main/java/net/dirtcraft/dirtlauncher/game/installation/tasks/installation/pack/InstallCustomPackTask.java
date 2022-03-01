package net.dirtcraft.dirtlauncher.game.installation.tasks.installation.pack;

import net.dirtcraft.dirtlauncher.configuration.ConfigurationManager;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.lib.data.tasks.DownloadTask;
import net.dirtcraft.dirtlauncher.lib.data.tasks.TaskExecutor;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;

public class InstallCustomPackTask implements IInstallationTask {

    private final Modpack pack;

    public InstallCustomPackTask(Modpack pack) {
        this.pack = pack;
    }

    public int getNumberSteps() {
        return 2;
    }

    @Override
    public void executeTask(ProgressContainer progressContainer, ConfigurationManager config) throws IOException {
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

        DownloadTask download = new DownloadTask(new URL(pack.getLink()), modpackZip);

        // Download the Pack
        TaskExecutor.execute(Collections.singletonList(download), progressContainer.showBitrate(), "Downloading ModPack");

        progressContainer.setNumMinorSteps(2);
        progressContainer.nextMajorStep();

        // Extract the pack
        progressContainer.setProgressText(String.format("Extracting %s Files", pack.getName()));
        new ZipFile(modpackZip).extractAll(modpackFolder.getPath());
        modpackZip.delete();

        progressContainer.nextMajorStep();
    }

    @Override
    public InstallationStages getRequiredStage() {
        return InstallationStages.INSTALL;
    }
}
