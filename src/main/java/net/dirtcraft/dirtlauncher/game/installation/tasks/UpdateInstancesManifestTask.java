package net.dirtcraft.dirtlauncher.game.installation.tasks;

import net.dirtcraft.dirtlauncher.configuration.Config;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.manifests.InstanceManifest;
import net.dirtcraft.dirtlauncher.utils.Manifests;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;

import java.util.*;
import java.util.concurrent.ExecutorService;

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
    public void executeTask(ExecutorService threadService, ProgressContainer progressContainer, Config config) {
        try {
            // Update Progress
            progressContainer.setProgressText("Updating Instances Manifest");
            progressContainer.setNumMinorSteps(2);

            InstanceManifest instanceManifest = Manifests.INSTANCE;

            InstanceManifest.Entry updated = new InstanceManifest.Entry(pack.getName(), pack.getVersion(), pack.getGameVersion(), pack.getForgeVersion());
            ListIterator<InstanceManifest.Entry> manifestIterator = instanceManifest.listIterator();

            while (true) {
                if (!manifestIterator.hasNext()) manifestIterator.add(updated);
                else if (manifestIterator.next().name.equals(pack.getName())) manifestIterator.set(updated);
                else continue;
                break;
            }

            progressContainer.completeMinorStep();

            // Update Instances Manifest
            instanceManifest.saveAsync();

            progressContainer.completeMinorStep();
            progressContainer.completeMajorStep();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public InstallationStages getRequiredStage() {
        return InstallationStages.POST_INSTALL;
    }
}
