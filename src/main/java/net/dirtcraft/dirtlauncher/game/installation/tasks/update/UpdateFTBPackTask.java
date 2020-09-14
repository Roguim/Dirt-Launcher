package net.dirtcraft.dirtlauncher.game.installation.tasks.update;

import net.dirtcraft.dirtlauncher.configuration.Config;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IUpdateTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

public class UpdateFTBPackTask implements IUpdateTask {

    private final Modpack pack;

    public UpdateFTBPackTask(Modpack pack) {
        this.pack = pack;
    }

    @Override
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
