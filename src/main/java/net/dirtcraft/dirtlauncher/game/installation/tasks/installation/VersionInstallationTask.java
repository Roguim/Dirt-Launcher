package net.dirtcraft.dirtlauncher.game.installation.tasks.installation;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.configuration.ConfigurationManager;
import net.dirtcraft.dirtlauncher.configuration.manifests.VersionManifest;
import net.dirtcraft.dirtlauncher.data.Minecraft.Rule;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.DownloadManager;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;
import net.dirtcraft.dirtlauncher.utils.WebUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.StreamSupport;

public class VersionInstallationTask implements IInstallationTask {

    private final JsonObject versionManifest;

    public VersionInstallationTask(JsonObject versionManifest) {
        this.versionManifest = versionManifest;
    }

    public int getNumberSteps() {
        return 2;
    }

    @Override
    public void executeTask(DownloadManager downloadManager, ProgressContainer progressContainer, ConfigurationManager config) throws IOException {
        // Manifest Breakdown
        String version = versionManifest.get("id").getAsString();

        // Update Progress
        progressContainer.setProgressText("Installing Minecraft " + version);
        progressContainer.setNumMinorSteps(2);

        // Prepare the version folder
        VersionManifest.Entry versionEntry = config.getVersionManifest().create(version);

        // Write the version JSON manifest
        JsonUtils.writeJsonToFile(versionEntry.getVersionManifestFile(), versionManifest);
        progressContainer.completeMinorStep();

        // Download jar
        WebUtils.copyURLToFile(versionManifest.getAsJsonObject("downloads").getAsJsonObject("client").get("url").getAsString(), versionEntry.getVersionJarFile());
        progressContainer.completeMinorStep();
        progressContainer.completeMajorStep();

        // Download Libraries
        progressContainer.setProgressText("Downloading Libraries");
        progressContainer.setNumMinorSteps(versionManifest.getAsJsonArray("libraries").size());

        File libsDir = versionEntry.getLibsFolder().toFile();
        File nativesDir = versionEntry.getNativesFolder().toFile();
        List<File> files = Collections.synchronizedList(new ArrayList<>());

        try {
            CompletableFuture.allOf(
                    StreamSupport.stream(versionManifest.getAsJsonArray("libraries").spliterator(), false)
                            .map(JsonElement::getAsJsonObject)
                            .map(library -> CompletableFuture.runAsync(() -> {
                                try {
                                    Optional<File> optPath = installLibrary(library, libsDir, nativesDir, progressContainer);
                                    optPath.ifPresent(files::add);
                                } catch (IOException e) {
                                    throw new CompletionException(e);
                                }
                            }, downloadManager.getThreadPool()))
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

        // Update Versions Manifest
        progressContainer.setProgressText("Updating Versions Manifest");

        versionEntry.addLibs(files);
        versionEntry.saveAsync();

        progressContainer.completeMajorStep();
    }

    private Optional<File> installLibrary(JsonObject library, File libDir, File nativeDir, ProgressContainer progress) throws IOException {
        File f = null;
        // Check if the library has conditions
        if (library.has("rules")) {
            @SuppressWarnings("UnstableApiUsage") List<Rule> rules = Main.gson.fromJson(library.get("rules"), new TypeToken<List<Rule>>(){}.getType());
            if (!rules.stream().allMatch(Rule::canDownload)) return Optional.empty();
        }

        JsonObject libraryDownloads = library.getAsJsonObject("downloads");

        // Download any standard libraries
        if (libraryDownloads.has("artifact")) {
            new File(libDir, StringUtils.substringBeforeLast(libraryDownloads.getAsJsonObject("artifact").get("path").getAsString(), "/").replace("/", File.separator)).mkdirs();
            String filePath = libDir.getPath() + File.separator + libraryDownloads.getAsJsonObject("artifact").get("path").getAsString().replace("/", File.separator);
            WebUtils.copyURLToFile(libraryDownloads.getAsJsonObject("artifact").get("url").getAsString(), new File(filePath));
            f = new File(filePath);
        }
        // Download any natives
        if (libraryDownloads.has("classifiers")) {
            String nativesType = "";
            if (SystemUtils.IS_OS_WINDOWS) nativesType = "natives-windows";
            else if (SystemUtils.IS_OS_MAC) nativesType = "natives-osx";
            else if (SystemUtils.IS_OS_LINUX) nativesType = "natives-linux";

            if (libraryDownloads.getAsJsonObject("classifiers").has(nativesType)) {
                JsonObject nativeJson = libraryDownloads.getAsJsonObject("classifiers").getAsJsonObject(nativesType);
                File outputFile = new File(nativeDir, nativeJson.get("sha1").getAsString());
                WebUtils.copyURLToFile(nativeJson.get("url").getAsString(), outputFile);
                FileUtils.extractJar(outputFile.getPath(), nativeDir.getPath());
                outputFile.delete();
            }
        }

        progress.completeMinorStep();
        return Optional.ofNullable(f);
    }

    @Override
    public InstallationStages getRequiredStage() {
        return InstallationStages.INSTALL;
    }
}