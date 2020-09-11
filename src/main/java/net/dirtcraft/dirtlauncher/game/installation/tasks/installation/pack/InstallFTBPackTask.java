package net.dirtcraft.dirtlauncher.game.installation.tasks.installation.pack;

import net.dirtcraft.dirtlauncher.Data.Config;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.gui.home.sidebar.Pack;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

public class InstallFTBPackTask implements IInstallationTask {

    private final Pack pack;

    public InstallFTBPackTask(Pack pack) {
        this.pack = pack;
    }

    // TODO Change
    public int getNumberSteps() {
        return 0;
    }

    @Override
    public void executeTask(ExecutorService threadService, ProgressContainer progressContainer, Config config) throws IOException {

    }
}
