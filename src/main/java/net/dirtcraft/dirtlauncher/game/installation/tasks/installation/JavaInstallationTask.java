package net.dirtcraft.dirtlauncher.game.installation.tasks.installation;

import net.dirtcraft.dirtlauncher.configuration.ConfigurationManager;
import net.dirtcraft.dirtlauncher.data.Minecraft.GameVersion;
import net.dirtcraft.dirtlauncher.data.Minecraft.JavaVersion;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.installation.tasks.PackInstallException;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.DownloadManager;

import java.io.IOException;

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

    }

    @Override
    public InstallationStages getRequiredStage() {
        return null;
    }
}
