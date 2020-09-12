package net.dirtcraft.dirtlauncher.game.installation.tasks;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.configuration.Config;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.utils.FileUtils;

import java.io.File;
import java.util.Iterator;
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
    public void executeTask(ExecutorService threadService, ProgressContainer progressContainer, Config config) {
        // Update Progress
        progressContainer.setProgressText("Updating Instances Manifest");
        progressContainer.setNumMinorSteps(2);

        JsonObject instanceManifest = FileUtils.readJsonFromFile(config.getDirectoryManifest(config.getInstancesDirectory()));
        JsonArray packsArray = instanceManifest.getAsJsonArray("packs");

        // Delete Old Entries
        Iterator<JsonElement> jsonIterator = packsArray.iterator();
        while(jsonIterator.hasNext()) {
            if(jsonIterator.next().getAsJsonObject().get("name").getAsString().equals(pack.getName())) jsonIterator.remove();
        }
        progressContainer.completeMinorStep();

        // Update Instances Manifest
        JsonObject packJson = new JsonObject();
        packJson.addProperty("name", pack.getName());
        packJson.addProperty("version", pack.getVersion());
        packJson.addProperty("gameVersion", pack.getGameVersion());
        packJson.addProperty("forgeVersion", pack.getForgeVersion());
        packsArray.add(packJson);
        FileUtils.writeJsonToFile(new File(config.getDirectoryManifest(config.getInstancesDirectory()).getPath()), instanceManifest);

        progressContainer.completeMinorStep();
        progressContainer.completeMajorStep();
    }

    @Override
    public InstallationStages getRequiredStage() {
        return InstallationStages.POST_INSTALL;
    }
}
