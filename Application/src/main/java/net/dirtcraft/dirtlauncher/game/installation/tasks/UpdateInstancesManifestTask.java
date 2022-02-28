package net.dirtcraft.dirtlauncher.game.installation.tasks;

import net.dirtcraft.dirtlauncher.DirtLauncher;
import net.dirtcraft.dirtlauncher.configuration.ConfigurationManager;
import net.dirtcraft.dirtlauncher.configuration.manifests.InstanceManifest;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;

public class UpdateInstancesManifestTask implements IInstallationTask {

    private final Modpack pack;

    public UpdateInstancesManifestTask(Modpack pack) {
        this.pack = pack;
    }

    @Override
    public int getNumberSteps() {
        return 1;
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void executeTask(ProgressContainer progressContainer, ConfigurationManager config) {
        try {
            // Update Progress
            progressContainer.setProgressText("Updating Instances Manifest");
            progressContainer.setNumMinorSteps(2);

            InstanceManifest instanceManifest = DirtLauncher.getConfig().getInstanceManifest();

            instanceManifest.update(pack);

            progressContainer.completeMinorStep();

            // Update Instances Manifest
            instanceManifest.saveAsync();

            progressContainer.completeMinorStep();
            progressContainer.nextMajorStep();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public InstallationStages getRequiredStage() {
        return InstallationStages.POST_INSTALL;
    }
}
