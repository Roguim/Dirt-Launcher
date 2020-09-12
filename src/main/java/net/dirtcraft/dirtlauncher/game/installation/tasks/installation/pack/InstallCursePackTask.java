package net.dirtcraft.dirtlauncher.game.installation.tasks.installation.pack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.therandomlabs.utils.io.NetUtils;
import net.dirtcraft.dirtlauncher.configuration.Config;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.dirtcraft.dirtlauncher.utils.WebUtils;
import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.StreamSupport;

public class InstallCursePackTask implements IInstallationTask {

    private final Modpack pack;

    public InstallCursePackTask(Modpack pack) {
        this.pack = pack;
    }

    public int getNumberSteps() {
        return 4;
    }

    @Override
    public void executeTask(ExecutorService threadService, ProgressContainer progressContainer, Config config) throws IOException {
        // Update Progress
        progressContainer.setProgressText("Downloading Modpack Files");
        progressContainer.setNumMinorSteps(2);

        // Prepare Folders
        final File modpackFolder = pack.getInstanceDirectory();
        final File modpackZip = new File(modpackFolder.getPath(), "modpack.zip");
        final File tempDir = new File(modpackFolder.getPath(), "temp");

        FileUtils.deleteDirectory(modpackFolder);
        modpackFolder.mkdirs();
        tempDir.mkdirs();

        progressContainer.completeMinorStep();

        // Download Modpack Zip
        FileUtils.copyURLToFile(NetUtils.getRedirectedURL(new URL(pack.getLink())).toString().replace("%2B", "+"), modpackZip);
        progressContainer.completeMinorStep();
        progressContainer.completeMajorStep();

        // Update Progress
        progressContainer.setProgressText(String.format("Extracting %s Files", pack.getName()));
        progressContainer.setNumMinorSteps(4);

        // Extract Modpack Zip
        new ZipFile(modpackZip).extractAll(tempDir.getPath());
        progressContainer.completeMinorStep();

        // Delete the Modpack Zip
        modpackZip.delete();
        progressContainer.completeMinorStep();

        // Sort out the files
        FileUtils.copyDirectory(new File(tempDir.getPath(), "overrides"), modpackFolder);
        JsonObject modpackManifest = FileUtils.readJsonFromFile(new File(tempDir, "manifest.json"));
        FileUtils.writeJsonToFile(new File(modpackFolder, "manifest.json"), modpackManifest);
        progressContainer.completeMinorStep();

        // Delete the temporary files
        FileUtils.deleteDirectory(tempDir);
        progressContainer.completeMinorStep();

        progressContainer.completeMajorStep();

        JsonArray mods = modpackManifest.getAsJsonArray("files");

        // Update Progress
        progressContainer.setProgressText("Preparing Mod Manifests");
        progressContainer.setNumMinorSteps(mods.size());

        List<JsonObject> modManifests = Collections.synchronizedList(new ArrayList());

        // Download Mod Manifests
        CompletableFuture.allOf(
                StreamSupport.stream(mods.spliterator(), false)
                        .map(JsonElement::getAsJsonObject)
                        .filter(mod -> !(mod.has("required") && !mod.get("required").getAsBoolean()))
                        .map(mod -> CompletableFuture.runAsync(() -> {
                            modManifests.add(WebUtils.getJsonFromUrl(String.format(
                                    "https://addons-ecs.forgesvc.net/api/v2/addon/%s/file/%s",
                                    mod.get("projectID").getAsString(),
                                    mod.get("fileID").getAsString())));

                            progressContainer.completeMinorStep();
                        }, threadService))
                        .toArray(CompletableFuture[]::new))
                .join();

        progressContainer.completeMajorStep();

        // Update Progress
        progressContainer.setProgressText("Downloading Mods");
        synchronized (modManifests) {
            progressContainer.setNumMinorSteps(modManifests.stream()
                    .mapToInt(obj -> obj.get("fileLength").getAsInt())
                    .sum());
        }

        // Install Mods
        File modsFolder = new File(modpackFolder.getPath(), "mods");
        modsFolder.mkdirs();

        synchronized (modManifests) {
            try {
                CompletableFuture.allOf(
                        modManifests.stream()
                        .map(mod -> CompletableFuture.runAsync(() -> {
                            try {
                                // Download the mod
                                FileUtils.copyURLToFile(mod.get("downloadUrl").getAsString().replaceAll("\\s", "%20"), new File(modsFolder, mod.get("fileName").getAsString()));

                                // Update progress
                                progressContainer.addMinorStepsCompleted(mod.get("fileLength").getAsInt());
                            } catch (IOException e) {
                                throw new CompletionException(e);
                            }
                        }, threadService))
                        .toArray(CompletableFuture[]::new))
                    .join();
            } catch (CompletionException e) {
                try {
                    throw e.getCause();
                } catch (IOException ex) {
                    throw ex;
                } catch (Throwable impossible) {
                    throw new AssertionError(impossible);
                }
            }
        }

        progressContainer.completeMajorStep();
    }

    @Override
    public InstallationStages getRequiredStage() {
        return InstallationStages.INSTALL;
    }
}
