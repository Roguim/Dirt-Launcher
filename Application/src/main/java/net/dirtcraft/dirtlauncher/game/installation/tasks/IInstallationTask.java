package net.dirtcraft.dirtlauncher.game.installation.tasks;

import net.dirtcraft.dirtlauncher.configuration.ConfigurationManager;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;

import java.io.IOException;

public interface IInstallationTask {

    // The number of major steps (bottom bar) contained within this task.
    int getNumberSteps();

    // Execute the task.
    void executeTask(ProgressContainer progressContainer, ConfigurationManager config) throws IOException, PackInstallException;

    InstallationStages getRequiredStage();

}
