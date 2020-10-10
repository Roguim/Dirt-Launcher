package net.dirtcraft.dirtlauncher.game.installation.tasks.installation;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.configuration.ConfigurationManager;
import net.dirtcraft.dirtlauncher.configuration.manifests.VersionManifest;
import net.dirtcraft.dirtlauncher.data.Minecraft.Download;
import net.dirtcraft.dirtlauncher.data.Minecraft.Library;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.installation.tasks.PackInstallException;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.DownloadManager;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.IPresetDownload;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.Result;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.progress.Trackers;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class VersionInstallationTask implements IInstallationTask {

    private final JsonObject versionManifest;

    public VersionInstallationTask(JsonObject versionManifest) {
        this.versionManifest = versionManifest;
    }

    public int getNumberSteps() {
        return 2;
    }

    @Override
    @SuppressWarnings({"UnstableApiUsage"})
    public void executeTask(DownloadManager downloadManager, ProgressContainer progressContainer, ConfigurationManager config) throws IOException {
        // Manifest Breakdown
        String version = versionManifest.get("id").getAsString();
        VersionManifest.Entry versionEntry = config.getVersionManifest().create(version);
        Path libsDir = versionEntry.getLibsFolder();
        Path nativesDir = versionEntry.getNativesFolder();
        Path tempDir = nativesDir.resolve("temp");

        // Update Progress
        progressContainer.setProgressText("Installing Minecraft " + version);
        progressContainer.setNumMinorSteps(2);

        // Write the version JSON manifest
        JsonUtils.writeJsonToFile(versionEntry.getVersionManifestFile(), versionManifest);
        progressContainer.completeMinorStep();

        downloadMinecraft(progressContainer, downloadManager, versionEntry);

        List<Library> rawLibs = Main.gson.fromJson(versionManifest.getAsJsonArray("libraries"), new TypeToken<List<Library>>(){}.getType());
        rawLibs.removeIf(Library::notRequired);

        List<Result> libs = downloadLibraries(rawLibs, libsDir, downloadManager, progressContainer);
        List<Result> natives = downloadNatives(rawLibs, tempDir, downloadManager, progressContainer);

        progressContainer.nextMajorStep("Extracting Natives", natives.size());
        Optional<IOException> extractionException = natives.stream()
                .map(r->extractNatives(r, nativesDir))
                .peek(e->progressContainer.completeMinorStep())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();

        if (extractionException.isPresent()) throw extractionException.get();

        progressContainer.setProgressText("Updating Versions Manifest");

        versionEntry.addLibs(libs);
        versionEntry.saveAsync();
        FileUtils.deleteDirectory(tempDir.toFile());
        FileUtils.deleteDirectory(nativesDir.resolve("META-INF").toFile());
        progressContainer.nextMajorStep();
    }

    @SuppressWarnings("UnstableApiUsage")
    private void downloadMinecraft(ProgressContainer progressContainer, DownloadManager downloadManager, VersionManifest.Entry versionEntry) throws PackInstallException {
        Type type = new TypeToken<Download>(){}.getType();
        Trackers.MultiUpdater updater = Trackers.getSimpleTracker(progressContainer, "Minecraft Version JAR");
        IPresetDownload jar = JsonUtils.getJsonElement(versionManifest, element -> Main.gson.<Download>fromJson(element, type), "downloads", "client")
                .map(download -> download.getPreset(versionEntry.getVersionJarFile()))
                .orElseThrow(() -> new PackInstallException("No minecraft download"));
        downloadManager.download(updater, jar);
    }

    private List<Result> downloadNatives(List<Library> libs, Path downloadFolder, DownloadManager downloadManager, ProgressContainer progressContainer) {
        Trackers.MultiUpdater updater = Trackers.getSimpleTracker(progressContainer, "Natives");
        List<IPresetDownload> natives = libs.stream()
                .map(Library::getNative)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(dl -> dl.getPreset(downloadFolder))
                .collect(Collectors.toList());

        return downloadManager.download(updater, natives);
    }

    private List<Result> downloadLibraries(List<Library> libs, Path downloadFolder, DownloadManager downloadManager, ProgressContainer progressContainer) {
        Trackers.MultiUpdater updater = Trackers.getSimpleTracker(progressContainer, "Libraries");
        List<IPresetDownload> libraries = libs.stream()
                .map(Library::getArtifact)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(dl -> dl.getPreset(downloadFolder))
                .collect(Collectors.toList());

        return downloadManager.download(updater, libraries);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private Optional<IOException> extractNatives(Result result, Path nativeDir){
        try {
            FileUtils.extractJar(result.getFile().getPath(), nativeDir.toString());
            result.getFile().delete();
            return Optional.empty();
        } catch (IOException e){
            return Optional.of(e);
        }
    }


    @Override
    public InstallationStages getRequiredStage() {
        return InstallationStages.INSTALL;
    }
}