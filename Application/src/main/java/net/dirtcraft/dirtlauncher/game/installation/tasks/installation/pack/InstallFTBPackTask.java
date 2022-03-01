package net.dirtcraft.dirtlauncher.game.installation.tasks.installation.pack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.therandomlabs.utils.io.NetUtils;
import net.dirtcraft.dirtlauncher.configuration.ConfigurationManager;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.lib.data.tasks.DownloadTask;
import net.dirtcraft.dirtlauncher.lib.data.tasks.Task;
import net.dirtcraft.dirtlauncher.lib.data.tasks.TaskExecutor;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;
import net.dirtcraft.dirtlauncher.utils.WebUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
    public void executeTask(ProgressContainer progressContainer, ConfigurationManager config) throws IOException {
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

        Stream<Task<?>> a = files.stream()
                .filter(file->!file.get("serveronly").getAsBoolean())
                .map(file->{
                        File dest = new File(new File(modpackFolder, file.get("path").getAsString()), file.get("name").getAsString());
                        URL src = MiscUtils.getURL(file.get("url").getAsString().replaceAll("\\s", "%20")).orElse(null);
                        long sz = file.get("size").getAsLong();
                        return new DownloadTask(src, dest, sz);
                });
        Collection<Task<?>> b = a.collect(TaskExecutor.collector(progressContainer.showBitrate(), "Downloading Modpack Files"));//a.collect(Collectors.toList());
        //TaskExecutor.execute(b, progressContainer.bitrate, "Downloading Modpack Files");
        progressContainer.nextMajorStep();

    }

    @Override
    public InstallationStages getRequiredStage() {
        return InstallationStages.INSTALL;
    }
}
