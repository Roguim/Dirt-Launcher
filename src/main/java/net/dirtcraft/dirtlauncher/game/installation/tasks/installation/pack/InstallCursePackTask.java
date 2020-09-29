package net.dirtcraft.dirtlauncher.game.installation.tasks.installation.pack;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.therandomlabs.utils.io.NetUtils;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.configuration.ConfigurationManager;
import net.dirtcraft.dirtlauncher.data.Curse.CurseMetaFileReference;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.Download;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.DownloadManager;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.Trackers;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;
import net.dirtcraft.dirtlauncher.utils.WebUtils;
import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.stream.StreamSupport;

public class InstallCursePackTask implements IInstallationTask {

    private final Modpack pack;

    public InstallCursePackTask(Modpack pack) {
        this.pack = pack;
    }

    @Override
    public void executeTask(ExecutorService threadService, ProgressContainer progressContainer, ConfigurationManager config) throws IOException {
        // Update Progress
        progressContainer.setProgressText("Downloading Modpack Files");
        progressContainer.setNumMinorSteps(2);

        // Prepare Folders
        final File modpackFolder = pack.getInstanceDirectory();
        final File modpackZip = new File(modpackFolder, "modpack.zip");
        final File tempDir = new File(modpackFolder, "temp");

        FileUtils.deleteDirectory(modpackFolder);
        modpackFolder.mkdirs();
        tempDir.mkdirs();

        progressContainer.completeMinorStep();

        // Download Modpack Zip
        WebUtils.copyURLToFile(NetUtils.getRedirectedURL(new URL(pack.getLink())).toString().replace("%2B", "+"), modpackZip);
        progressContainer.completeMinorStep();
        progressContainer.completeMajorStep();

        // Update Progress
        progressContainer.setProgressText(String.format("Extracting %s Files", pack.getName()));
        progressContainer.setNumMinorSteps(4);

        // Extract Modpack Zip
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
        //FileUtils.writeJsonToFile(new File(modpackFolder, "manifest.json"), modpackManifest);
        progressContainer.completeMinorStep();

        // Delete the temporary files
        FileUtils.deleteDirectory(tempDir);
        progressContainer.completeMinorStep();

        progressContainer.completeMajorStep();

        JsonArray mods = modpackManifest.getAsJsonArray("files");

        // Update Progress
        progressContainer.setProgressText("Preparing Mod Manifests");

        // Download & Install Mods
        File modsFile = new File(modpackFolder.getPath(), "mods");
        Path modsFolder = modsFile.toPath();
        modsFile.mkdirs();

        Gson gson = Main.gson;
        DownloadManager manager = new DownloadManager();
        StreamSupport.stream(mods.spliterator(), false)
                .map(f->gson.fromJson(f, CurseMetaFileReference.class))
                .filter(CurseMetaFileReference::isRequired)
                .map(meta->meta.getDownloadInfo(modsFolder))
                .forEach(manager::addDownload);

        List<Download.Result> results = manager.download(Trackers.getProgressContainerTracker(progressContainer, 50, 3), 50);
        Optional<Throwable> err = results.stream()
                .filter(Download.Result::finishedExceptionally)
                .findFirst()
                .flatMap(Download.Result::getException);

        if (err.isPresent()) throw new IOException(err.get());
        progressContainer.completeMajorStep();
    }

    @Override
    public int getNumberSteps() {
        return 4;
    }

    @Override
    public InstallationStages getRequiredStage() {
        return InstallationStages.INSTALL;
    }
}
