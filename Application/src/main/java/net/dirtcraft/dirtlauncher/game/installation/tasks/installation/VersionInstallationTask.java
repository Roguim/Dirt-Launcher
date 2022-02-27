package net.dirtcraft.dirtlauncher.game.installation.tasks.installation;

import net.dirtcraft.dirtlauncher.configuration.ConfigurationManager;
import net.dirtcraft.dirtlauncher.configuration.manifests.VersionManifest;
import net.dirtcraft.dirtlauncher.data.Minecraft.GameVersion;
import net.dirtcraft.dirtlauncher.data.Minecraft.Library;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.installation.tasks.PackInstallException;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.DownloadManager;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.IFileDownload;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.Result;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.progress.Trackers;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class VersionInstallationTask implements IInstallationTask {

    private final GameVersion versionManifest;

    public VersionInstallationTask(GameVersion versionManifest) {
        this.versionManifest = versionManifest;
    }

    public int getNumberSteps() {
        return 2;
    }

    @Override
    public void executeTask(DownloadManager downloadManager, ProgressContainer progressContainer, ConfigurationManager config) throws IOException {
        // Manifest Breakdown
        VersionManifest.Entry versionEntry = config.getVersionManifest().create(versionManifest.getId(), versionManifest.getJava());
        Path libsDir = versionEntry.getLibsFolder();
        Path nativesDir = versionEntry.getNativesFolder();
        Path tempDir = nativesDir.resolve("temp");

        // Update Progress
        progressContainer.setProgressText("Installing Minecraft " + versionManifest.getId());
        progressContainer.setNumMinorSteps(2);

        // Write the version JSON manifest
        JsonUtils.toJson(versionEntry.getVersionManifestFile(), versionManifest, GameVersion.class);
        progressContainer.completeMinorStep();

        downloadMinecraft(progressContainer, downloadManager, versionEntry);

        List<Library> rawLibs = versionManifest.getLibraries();
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

    private void downloadMinecraft(ProgressContainer progressContainer, DownloadManager downloadManager, VersionManifest.Entry versionEntry) throws PackInstallException {
        Trackers.MultiUpdater updater = Trackers.getSimpleTracker(progressContainer, "Minecraft Version JAR");
        IFileDownload jar = versionManifest.getDownload("client").getPreset(versionEntry.getVersionJarFile());
        downloadManager.download(updater, jar);
    }

    private List<Result> downloadNatives(List<Library> libs, Path downloadFolder, DownloadManager downloadManager, ProgressContainer progressContainer) {
        Trackers.MultiUpdater updater = Trackers.getSimpleTracker(progressContainer, "Natives");
        List<IFileDownload> natives = libs.stream()
                .map(Library::getNative)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(dl -> dl.getPreset(downloadFolder))
                .collect(Collectors.toList());

        return downloadManager.download(updater, natives);
    }

    private List<Result> downloadLibraries(List<Library> libs, Path downloadFolder, DownloadManager downloadManager, ProgressContainer progressContainer) {
        Trackers.MultiUpdater updater = Trackers.getSimpleTracker(progressContainer, "Libraries");
        List<IFileDownload> libraries = libs.stream()
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