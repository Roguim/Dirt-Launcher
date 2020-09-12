package net.dirtcraft.dirtlauncher.game.installation.tasks.installation.pack;

import net.dirtcraft.dirtlauncher.configuration.Config;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

public class InstallFTBPackTask implements IInstallationTask {

    private final Modpack pack;

    public InstallFTBPackTask(Modpack pack) {
        this.pack = pack;
    }

    // TODO Change
    public int getNumberSteps() {
        return 0;
    }

    @Override
    public void executeTask(ExecutorService threadService, ProgressContainer progressContainer, Config config) throws IOException {

    }

    @Override
    public InstallationStages getRequiredStage() {
        return InstallationStages.INSTALL;
    }
}
