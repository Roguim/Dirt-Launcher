package net.dirtcraft.dirtlauncher.game.installation.tasks.installation.pack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.therandomlabs.utils.io.NetUtils;
import net.dirtcraft.dirtlauncher.configuration.ConfigurationManager;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.DownloadManager;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;
import net.dirtcraft.dirtlauncher.utils.WebUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class InstallFTBPackTask implements IInstallationTask {

    private final Modpack pack;

    public InstallFTBPackTask(Modpack pack) {
        this.pack = pack;
    }

    public int getNumberSteps() {
        return 2;
    }

    @Override
    public void executeTask(DownloadManager downloadManager, ProgressContainer progressContainer, ConfigurationManager config) throws IOException {
        // Update Progress
        progressContainer.setProgressText("Preparing Modpack Manifest");
        progressContainer.setNumMinorSteps(2);

        // Prepare Folders
        final File modpackFolder = pack.getInstanceDirectory();

        FileUtils.deleteDirectory(modpackFolder);
        File modpackManifestFile = new File(pack.getInstanceDirectory(), "manifest.json");
        modpackFolder.mkdirs();
        modpackManifestFile.createNewFile();

        progressContainer.completeMinorStep();

        //Prepare Modpack Manifests
        JsonObject packJson = WebUtils.getJsonFromUrl(pack.getLink());

        // Find Correct Modpack Version
        JsonArray versions = packJson.getAsJsonArray("versions");
        Optional<JsonElement> version = StreamSupport.stream(versions
                .spliterator(), false)
                .filter(v -> v.getAsJsonObject().get("name").getAsString().equalsIgnoreCase(pack.getVersion()))
                .findFirst();
        int latestVersionId = version.map(jsonElement -> jsonElement.getAsJsonObject().get("id").getAsInt())
                .orElseGet(() -> StreamSupport.stream(versions.spliterator(), false)
                .filter(v -> v.getAsJsonObject().get("type").getAsString().equalsIgnoreCase("release"))
                .collect(Collectors.toList()).get(0).getAsJsonObject().get("id").getAsInt());

        final String packLink = NetUtils.getRedirectedURL(new URL(pack.getLink() + "/" + latestVersionId)).toString();

        JsonObject modpackManifest = WebUtils.getJsonFromUrl(packLink);
        JsonUtils.writeJsonToFile(modpackManifestFile, modpackManifest);
        JsonArray filesArray = modpackManifest.getAsJsonArray("files");
        List<JsonObject> files = Collections.synchronizedList(new ArrayList<>());
        for (JsonElement fileElement : filesArray) files.add(fileElement.getAsJsonObject());
        progressContainer.completeMinorStep();
        progressContainer.nextMajorStep();

        synchronized (files) {
            progressContainer.setNumMinorSteps(
                    files.stream()
                    .mapToInt(element -> element.get("size").getAsInt())
                    .sum());
        }

        progressContainer.setProgressText("Downloading Modpack Files");
        //Download Modpack Files
        synchronized (files) {
            try {
                CompletableFuture.allOf(
                        StreamSupport.stream(files.spliterator(), false)
                                .map(JsonElement::getAsJsonObject)
                                .filter(file -> !file.get("serveronly").getAsBoolean())
                                .map(file -> CompletableFuture.runAsync(() -> {
                                    try {
                                        int size = file.get("size").getAsInt();
                                        String url = file.get("url").getAsString().replaceAll("\\s", "%20");
                                        File path = new File(new File(modpackFolder, file.get("path").getAsString()), file.get("name").getAsString());

                                        WebUtils.copyURLToFile(url, path);
                                        progressContainer.addMinorStepsCompleted(size);
                                    } catch (IOException e) {
                                        throw new CompletionException(e);
                                    }
                                }, downloadManager.getThreadPool()))
                                .toArray(CompletableFuture[]::new)
                ).join();
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

        progressContainer.nextMajorStep();

    }

    @Override
    public InstallationStages getRequiredStage() {
        return InstallationStages.INSTALL;
    }
}
