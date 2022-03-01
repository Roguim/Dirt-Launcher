package net.dirtcraft.dirtlauncher.game.installation.tasks.installation.pack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.DirtLauncher;
import net.dirtcraft.dirtlauncher.configuration.ConfigurationManager;
import net.dirtcraft.dirtlauncher.lib.data.json.curse.CurseFile;
import net.dirtcraft.dirtlauncher.lib.data.json.curse.CurseMetaFileReference;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.lib.data.tasks.DownloadTask;
import net.dirtcraft.dirtlauncher.lib.data.tasks.JsonTask;
import net.dirtcraft.dirtlauncher.lib.data.tasks.Task;
import net.dirtcraft.dirtlauncher.lib.data.tasks.TaskExecutor;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;
import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class InstallCursePackTask implements IInstallationTask {

    private final Modpack pack;

    public InstallCursePackTask(Modpack pack) {
        this.pack = pack;
    }

    @Override
    public void executeTask(ProgressContainer progressContainer, ConfigurationManager config) throws IOException {
        // Prepare Folders
        final File modpackFolder = pack.getInstanceDirectory();
        final File modpackZip = new File(modpackFolder, "modpack.zip");
        final File tempDir = new File(modpackFolder, "temp");
        FileUtils.deleteDirectory(modpackFolder);
        modpackFolder.mkdirs();
        tempDir.mkdirs();

        // Download Modpack Zip
        DownloadTask manifestDownload = new DownloadTask(new URL(new URL(pack.getLink()).toString().replace("%2B", "+")), modpackZip);
        TaskExecutor.execute(Collections.singleton(manifestDownload), progressContainer.showBitrate(), "Downloading Manifest");

        // Extract Modpack Zip
        progressContainer.nextMajorStep(String.format("Extracting %s Files", pack.getName()), 4);
        new ZipFile(modpackZip).extractAll(tempDir.getPath());
        progressContainer.completeMinorStep();

        // Delete the Modpack Zip
        modpackZip.delete();
        progressContainer.completeMinorStep();

        // Sort out the files
        FileUtils.copyDirectory(new File(tempDir.getPath(), "overrides"), modpackFolder);
        File tempManifest = new File(tempDir, "manifest.json");
        JsonObject modpackManifest = JsonUtils.readJsonFromFile(tempManifest);
        org.apache.commons.io.FileUtils.copyFile(tempManifest, new File(modpackFolder, "manifest.json"));
        progressContainer.completeMinorStep();

        // Delete the temporary files
        FileUtils.deleteDirectory(tempDir);
        progressContainer.completeMinorStep();

        progressContainer.nextMajorStep();

        JsonArray mods = modpackManifest.getAsJsonArray("files");

        // Update Progress
        progressContainer.setProgressText("Preparing Mod Manifests");

        // Download & Install Mods
        File modsFile = new File(modpackFolder.getPath(), "mods");
        Path modsFolder = modsFile.toPath();
        modsFile.mkdirs();

        DownloadTask exception = StreamSupport.stream(mods.spliterator(), false)
                .map(f-> DirtLauncher.getGson().fromJson(f, CurseMetaFileReference.class))
                .filter(CurseMetaFileReference::isRequired)
                .map(x-> MiscUtils.getURL(x.getDownloadUrl()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(x->new JsonTask<>(x, CurseFile.class))
                .collect(TaskExecutor.collector(progressContainer.showProgress(), "Fetching downloads"))
                .stream()
                .map(JsonTask::getResult)
                .map(x->x.getDownload(modsFolder.toFile()))
                .collect(TaskExecutor.collector(progressContainer.showBitrate(), "Downloading Mods"))
                .stream()
                .filter(Task::completedExceptionally)
                .findFirst().orElse(null);
        if (exception != null) exception.throwException();
        progressContainer.nextMajorStep();
    }

    @Override
    public int getNumberSteps() {
        return 3;
    }

    @Override
    public InstallationStages getRequiredStage() {
        return InstallationStages.INSTALL;
    }
}
