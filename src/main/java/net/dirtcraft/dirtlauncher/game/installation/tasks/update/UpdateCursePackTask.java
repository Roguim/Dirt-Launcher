package net.dirtcraft.dirtlauncher.game.installation.tasks.update;

import com.google.common.reflect.TypeToken;
import net.dirtcraft.dirtlauncher.configuration.ConfigurationManager;
import net.dirtcraft.dirtlauncher.data.Curse.CurseModpackManifest;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IUpdateTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.DownloadManager;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.DownloadMeta;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.DownloadTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.IDownload;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.IPresetDownload;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.progress.Trackers;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;
import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateCursePackTask implements IUpdateTask {

    private final Modpack pack;
    private final File modpackFolder;
    private final File modpackZip;
    private final File tempDir;

    public UpdateCursePackTask(Modpack pack) {
        this.pack = pack;
        this.modpackFolder = pack.getInstanceDirectory();
        this.modpackZip = new File(modpackFolder, "modpack.zip");
        this.tempDir = new File(modpackFolder, "temp");
        if (tempDir.exists()) FileUtils.deleteDirectoryUnchecked(tempDir);
    }

    @SuppressWarnings({"UnstableApiUsage", "ResultOfMethodCallIgnored"})
    @Override
    public void executeTask(DownloadManager downloadManager, ProgressContainer progressContainer, ConfigurationManager config) throws IOException {
        // Prepare Folders
        modpackFolder.mkdirs();
        tempDir.mkdirs();

        // Download Modpack Zip
        IPresetDownload manifestDownload = new DownloadMeta(new URL(pack.getLink()).toString().replace("%2B", "+"), modpackZip);
        Trackers.MultiUpdater updater = Trackers.getTracker(progressContainer, "Preparing Download", "Downloading Manifest");
        downloadManager.download(updater, manifestDownload);

        // Extract Modpack Zip
        progressContainer.nextMajorStep(String.format("Extracting %s Files", pack.getName()), 3);
        new ZipFile(modpackZip).extractAll(tempDir.getPath());
        progressContainer.completeMinorStep();

        // Delete the Modpack Zip
        modpackZip.delete();
        progressContainer.completeMinorStep();

        // Sort out the files
        FileUtils.copyDirectory(new File(tempDir, "overrides"), modpackFolder);
        File modsFolder = new File(modpackFolder, "mods");
        File tempManifestFile = new File(tempDir, "manifest.json");
        File currentManifestFile = new File(modpackFolder, "manifest.json");
        CurseModpackManifest oldManifest = JsonUtils.parseJsonUnchecked(currentManifestFile, new TypeToken<CurseModpackManifest>() {});
        CurseModpackManifest newManifest = JsonUtils.parseJsonUnchecked(tempManifestFile, new TypeToken<CurseModpackManifest>() {});
        modsFolder.mkdirs();
        progressContainer.completeMinorStep();

        //Work out what changes need to be made to the mods and remove / add them.
        progressContainer.nextMajorStep("Calculating Changes", oldManifest.files.size() + newManifest.files.size());
        List<IDownload> toRemove = oldManifest.files.parallelStream()
                .filter(file->file.required)
                .filter(file->newManifest.files.stream().noneMatch(file::equals))
                .peek(e->progressContainer.completeMinorStep())
                .collect(Collectors.toList());

        List<IDownload> toInstall = newManifest.files.parallelStream()
                .filter(file->file.required)
                .filter(file->oldManifest.files.stream().noneMatch(file::equals))
                .peek(e->progressContainer.completeMinorStep())
                .collect(Collectors.toList());

        //remove old mods
        progressContainer.nextMajorStep();
        Trackers.PreparationUpdater removalUpdater = Trackers.getPrepTracker(progressContainer, "Deleting outdated mods");
        downloadManager.preCalculate(removalUpdater, toRemove, modsFolder.toPath()).stream()
                .map(DownloadTask::getFile)
                .forEach(File::delete);

        //install new mods
        progressContainer.nextMajorStep();
        Trackers.MultiUpdater installUpdater = Trackers.getTracker(progressContainer, "Preparing to download new mods", "Downloading new mods");
        downloadManager.download(installUpdater, toInstall, modsFolder.toPath());


        // Delete the temporary files
        progressContainer.nextMajorStep("Cleaning Up", 1);
        org.apache.commons.io.FileUtils.copyFile(tempManifestFile, currentManifestFile);
        FileUtils.deleteDirectory(tempDir);
        progressContainer.completeMinorStep();
        progressContainer.nextMajorStep();
    }

    @Override
    public int getNumberSteps() {
        return 6;
    }

    @Override
    public InstallationStages getRequiredStage() {
        return InstallationStages.INSTALL;
    }
}
