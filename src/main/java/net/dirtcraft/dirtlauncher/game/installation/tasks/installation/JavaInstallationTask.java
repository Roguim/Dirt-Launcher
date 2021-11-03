package net.dirtcraft.dirtlauncher.game.installation.tasks.installation;

import net.dirtcraft.dirtlauncher.configuration.ConfigurationManager;
import net.dirtcraft.dirtlauncher.data.Minecraft.GameVersion;
import net.dirtcraft.dirtlauncher.data.Minecraft.Java.JavaManifest;
import net.dirtcraft.dirtlauncher.data.Minecraft.JavaVersion;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.installation.tasks.PackInstallException;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.DownloadManager;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.IFileDownload;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.progress.Trackers;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class JavaInstallationTask implements IInstallationTask {

    private final JavaVersion javaVersion;

    public JavaInstallationTask(GameVersion versionManifest) {
        this.javaVersion = versionManifest.getJava();
    }

    public JavaInstallationTask(JavaVersion javaVersion) {
        this.javaVersion = javaVersion;
    }

    @Override
    public int getNumberSteps() {
        return 0;
    }

    @Override
    public void executeTask(DownloadManager downloadManager, ProgressContainer progressContainer, ConfigurationManager config) throws IOException, PackInstallException {
        File jvmFolder = new File(config.getJavaDirectory(), javaVersion.component);
        List<IFileDownload> files = JavaManifest.getManifest()
                .flatMap(x->x.getVersionManifest(javaVersion.component))
                .map(x->x.getDownloads(jvmFolder))
                .map(dll->dll.stream().map(IFileDownload.class::cast).collect(Collectors.toList()))
                .get();
        Trackers.MultiUpdater tracker = Trackers.getTracker(progressContainer, "Calculating Download", "Downloading Java");
        downloadManager.download(tracker, files);
    }

    @Override
    public InstallationStages getRequiredStage() {
        return null;
    }
}
