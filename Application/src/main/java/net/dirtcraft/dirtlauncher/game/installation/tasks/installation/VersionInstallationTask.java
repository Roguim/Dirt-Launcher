package net.dirtcraft.dirtlauncher.game.installation.tasks.installation;

import net.dirtcraft.dirtlauncher.configuration.ConfigurationManager;
import net.dirtcraft.dirtlauncher.configuration.manifests.VersionManifest;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.installation.tasks.PackInstallException;
import net.dirtcraft.dirtlauncher.lib.data.json.mojang.GameVersion;
import net.dirtcraft.dirtlauncher.lib.data.json.mojang.Library;
import net.dirtcraft.dirtlauncher.lib.data.tasks.DownloadTask;
import net.dirtcraft.dirtlauncher.lib.data.tasks.ExtractTask;
import net.dirtcraft.dirtlauncher.lib.data.tasks.TaskExecutor;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
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
    public void executeTask(ProgressContainer progressContainer, ConfigurationManager config) throws IOException {
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

        downloadMinecraft(progressContainer, versionEntry);

        List<Library> rawLibs = versionManifest.getLibraries();
        rawLibs.removeIf(Library::notRequired);

        Collection<DownloadTask> libs = downloadLibraries(rawLibs, libsDir, progressContainer);
        Collection<DownloadTask> natives = downloadNatives(rawLibs, tempDir, progressContainer);

        progressContainer.nextMajorStep();
        List<ExtractTask> extractionTasks = natives.stream()
                .flatMap(x-> ExtractTask.from(x.getResult(), nativesDir).stream())
                .collect(Collectors.toList());
        TaskExecutor.execute(extractionTasks, progressContainer.showBitrate(), "Extracting Natives");

        progressContainer.setProgressText("Updating Versions Manifest");

        versionEntry.addDownloadedLibs(libs);
        versionEntry.saveAsync();
        FileUtils.deleteDirectory(tempDir.toFile());
        FileUtils.deleteDirectory(nativesDir.resolve("META-INF").toFile());
        progressContainer.nextMajorStep();
    }

    private void downloadMinecraft(ProgressContainer progressContainer, VersionManifest.Entry versionEntry) throws PackInstallException {
        DownloadTask jar = versionManifest.getDownload("client", versionEntry.getVersionJarFile());
        TaskExecutor.execute(Collections.singleton(jar), progressContainer.showBitrate(), "Minecraft Version JAR");
    }

    private Collection<DownloadTask> downloadNatives(List<Library> libs, Path downloadFolder, ProgressContainer progressContainer) {
        List<DownloadTask> natives = libs.stream()
                .map(Library::getNative)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(dl -> dl.getDownload(downloadFolder.toFile()))
                .collect(Collectors.toList());
        return TaskExecutor.execute(natives, progressContainer.showBitrate(), "Natives");
    }

    private Collection<DownloadTask> downloadLibraries(List<Library> libs, Path downloadFolder, ProgressContainer progressContainer) {
        List<DownloadTask> libraries = libs.stream()
                .map(Library::getArtifact)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(dl -> dl.getDownload(downloadFolder.toFile()))
                .collect(Collectors.toList());

        return TaskExecutor.execute(libraries, progressContainer.showBitrate(), "Libraries");
    }


    @Override
    public InstallationStages getRequiredStage() {
        return InstallationStages.INSTALL;
    }
}