package net.dirtcraft.dirtlauncher.game.installation.tasks.update;

import com.therandomlabs.utils.io.ZipFile;
import net.dirtcraft.dirtlauncher.configuration.ConfigurationManager;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IUpdateTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.DownloadManager;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.DownloadMeta;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.IFileDownload;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.progress.Trackers;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.utils.FileUtils;

import java.io.File;
import java.io.IOException;

public class UpdateCustomPackTask implements IUpdateTask {

    private final Modpack pack;
    private final File modpackFolder;
    private final File modpackZip;
    private final File tempDir;
    private final File configsDir;
    private final File modsDir;

    public UpdateCustomPackTask(Modpack pack) {
        this.pack = pack;
        this.modpackFolder = pack.getInstanceDirectory();
        this.modpackZip =   new File(modpackFolder, "modpack.zip");
        this.tempDir =      new File(modpackFolder, "temp");
        this.modsDir =      new File(modpackFolder, "mods");
        this.configsDir = new File(modpackFolder, "config");
        if (tempDir.exists()) tempDir.delete();
    }

    @Override
    public int getNumberSteps() {
        return 0;
    }

    @Override
    public void executeTask(DownloadManager downloadManager, ProgressContainer progressContainer, ConfigurationManager config) throws IOException {

        // Update Progress
        progressContainer.nextMajorStep();
        progressContainer.setProgressText("Downloading Update...");
        progressContainer.setNumMinorSteps(1);

        //Download update
        IFileDownload download = new DownloadMeta(pack.getLink(), modpackZip);
        downloadManager.download(Trackers.getTracker(progressContainer, "Preparing download...", "Downloading ModPack"), download);
        progressContainer.completeMinorStep();

        // Update Progress
        progressContainer.nextMajorStep();
        progressContainer.setProgressText("Deleting Old Files...");
        progressContainer.setNumMinorSteps(2);

        // Delete the existing mods and configs
        FileUtils.deleteDirectory(modsDir);
        progressContainer.completeMinorStep();
        FileUtils.deleteDirectory(configsDir);
        progressContainer.completeMinorStep();


        // Update Progress
        progressContainer.nextMajorStep();
        progressContainer.setProgressText("Adding Updated Files...");
        progressContainer.setNumMinorSteps(1);

        // Place in the new mods and configs
        new ZipFile(modpackZip.toPath()).extractAll(modpackFolder.toPath());
        modpackZip.delete();
        progressContainer.completeMinorStep();

        // Update Progress
        progressContainer.nextMajorStep();
        progressContainer.setProgressText("Cleaning Up...");
        progressContainer.setNumMinorSteps(1);

        // Delete the temporary files
        FileUtils.deleteDirectory(tempDir);
        progressContainer.completeMinorStep();
        progressContainer.nextMajorStep();
    }

    @Override
    public InstallationStages getRequiredStage() {
        return InstallationStages.INSTALL;
    }
}
