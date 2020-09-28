package net.dirtcraft.dirtlauncher.game.installation.tasks.installation.pack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.therandomlabs.utils.io.NetUtils;
import net.dirtcraft.dirtlauncher.configuration.ConfigurationManager;
import net.dirtcraft.dirtlauncher.game.installation.DownloadManager;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;
import net.dirtcraft.dirtlauncher.utils.WebUtils;
import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class InstallCursePackTask implements IInstallationTask {

    private final Modpack pack;

    public InstallCursePackTask(Modpack pack) {
        this.pack = pack;
    }

    @Override
    public void executeTask(ExecutorService threadService, ProgressContainer progressContainer, ConfigurationManager config) throws IOException {
        // Update Progress
        progressContainer.setProgressText("Downloading Modpack Files");
        progressContainer.setNumMinorSteps(2);

        // Prepare Folders
        final File modpackFolder = pack.getInstanceDirectory();
        final File modpackZip = new File(modpackFolder, "modpack.zip");
        final File tempDir = new File(modpackFolder, "temp");

        FileUtils.deleteDirectory(modpackFolder);
        modpackFolder.mkdirs();
        tempDir.mkdirs();

        progressContainer.completeMinorStep();

        // Download Modpack Zip
        WebUtils.copyURLToFile(NetUtils.getRedirectedURL(new URL(pack.getLink())).toString().replace("%2B", "+"), modpackZip);
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
        File tempManifest = new File(tempDir, "manifest.json");
        JsonObject modpackManifest = JsonUtils.readJsonFromFile(tempManifest);
        org.apache.commons.io.FileUtils.copyFile(tempManifest, new File(modpackFolder, "manifest.json"));
        //FileUtils.writeJsonToFile(new File(modpackFolder, "manifest.json"), modpackManifest);
        progressContainer.completeMinorStep();

        // Delete the temporary files
        FileUtils.deleteDirectory(tempDir);
        progressContainer.completeMinorStep();

        progressContainer.completeMajorStep();

        JsonArray mods = modpackManifest.getAsJsonArray("files");

        // Update Progress
        progressContainer.setProgressText("Preparing Mod Manifests");
        progressContainer.setNumMinorSteps(mods.size());

        List<JsonObject> modManifests = Collections.synchronizedList(new ArrayList<>());

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

        AtomicInteger i = new AtomicInteger();
        int samples = 10;
        long[] bytesPerSecond = new long[samples];
        Arrays.fill(bytesPerSecond, 0);
        DownloadManager manager = new DownloadManager(progress -> {
            int j = i.addAndGet(1) % samples;
            bytesPerSecond[j] = progress.getBytesPerSecond();
            progressContainer.setMinorPercent(progress.getPercent());
            if (j == 0 || progress.totalSize == 0) return;
            double sampledSpeed = Arrays.stream(bytesPerSecond).average().orElse(0d);
            String speed = sampledSpeed > DownloadManager.KILOBYTE ? String.format("%.1fMB/s", sampledSpeed / DownloadManager.MEGABYTE) : String.format("%dKB/s", (long) sampledSpeed / DownloadManager.KILOBYTE);
            final String template = "Downloading %dMB at " + speed;
            progressContainer.setProgressText(String.format(template, progress.totalSize / DownloadManager.MEGABYTE));
        }, 50);

        Collection<CompletableFuture<File>> files = modManifests.stream().map(mod -> {
            String url = mod.get("downloadUrl").getAsString().replaceAll("\\s", "%20");
            File file = new File(modsFolder, mod.get("fileName").getAsString());
            long size = mod.get("fileLength").getAsLong();
            return manager.download(url, file, size);
        }).collect(Collectors.toList());

        if (files.stream().map(CompletableFuture::join).anyMatch(Objects::isNull)) throw new IOException();
        manager.close();


        progressContainer.completeMajorStep();

    }

    @Override
    public int getNumberSteps() {
        return 4;
    }

    @Override
    public InstallationStages getRequiredStage() {
        return InstallationStages.INSTALL;
    }
}
