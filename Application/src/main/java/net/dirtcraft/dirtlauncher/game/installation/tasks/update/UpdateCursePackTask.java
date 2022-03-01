package net.dirtcraft.dirtlauncher.game.installation.tasks.update;

import com.google.common.reflect.TypeToken;
import net.dirtcraft.dirtlauncher.configuration.ConfigurationManager;
import net.dirtcraft.dirtlauncher.lib.data.json.curse.CurseFile;
import net.dirtcraft.dirtlauncher.lib.data.json.curse.CurseMetaFileReference;
import net.dirtcraft.dirtlauncher.lib.data.json.curse.CurseModpackManifest;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IUpdateTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.lib.data.tasks.DownloadTask;
import net.dirtcraft.dirtlauncher.lib.data.tasks.FileTask;
import net.dirtcraft.dirtlauncher.lib.data.tasks.JsonTask;
import net.dirtcraft.dirtlauncher.lib.data.tasks.TaskExecutor;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;
import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
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
    public void executeTask(ProgressContainer progressContainer, ConfigurationManager config) throws IOException {
        // Prepare Folders
        modpackFolder.mkdirs();
        tempDir.mkdirs();

        // Download Modpack Zip
        DownloadTask manifestDownload = new DownloadTask(new URL(new URL(pack.getLink()).toString().replace("%2B", "+")), modpackZip);
        TaskExecutor.execute(Collections.singleton(manifestDownload), progressContainer.showBitrate(), "Downloading Manifest");

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
        oldManifest.files.parallelStream()
                .filter(file->file.required)
                .filter(file->newManifest.files.stream().noneMatch(file::equals))
                .peek(e->progressContainer.completeMinorStep())
                .map(CurseMetaFileReference::getManifest)
                .collect(TaskExecutor.collector(progressContainer.showProgress(), "Fetching File MetaData"))
                .stream()
                .map(JsonTask::getResult)
                .map(x->x.getDownload(modsFolder))
                .map(FileTask::getDestination)
                .forEach(File::delete);

        newManifest.files.parallelStream()
                .filter(file->file.required)
                .filter(file->oldManifest.files.stream().noneMatch(file::equals))
                .peek(e->progressContainer.completeMinorStep())
                .map(CurseMetaFileReference::getManifest)
                .collect(TaskExecutor.collector(progressContainer.showProgress(), "Fetching File MetaData"))
                .stream()
                .map(JsonTask::getResult)
                .map(x->x.getDownload(modsFolder))
                .collect(TaskExecutor.collector(progressContainer.showBitrate(), "Downloading new mods"));

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
