package net.dirtcraft.dirtlauncher.game.installation.tasks.installation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.configuration.Config;
import net.dirtcraft.dirtlauncher.configuration.Manifests;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.manifests.VersionManifest;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.logging.Logger;
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
    public void executeTask(ExecutorService threadService, ProgressContainer progressContainer, Config config) throws IOException {
        // Manifest Breakdown
        String version = versionManifest.get("id").getAsString();

        // Update Progress
        progressContainer.setProgressText("Installing Minecraft " + version);
        progressContainer.setNumMinorSteps(2);

        // Prepare the version folder
        File versionFolder = new File(config.getVersionsDirectory(), version);
        FileUtils.deleteDirectory(versionFolder);
        versionFolder.mkdirs();

        // Write the version JSON manifest
        JsonUtils.writeJsonToFile(versionFolder.toPath().resolve(version + ".json").toFile(), versionManifest);
        progressContainer.completeMinorStep();

        // Download jar
        WebUtils.copyURLToFile(versionManifest.getAsJsonObject("downloads").getAsJsonObject("client").get("url").getAsString(), new File(versionFolder.getPath(), version + ".jar"));
        progressContainer.completeMinorStep();
        progressContainer.completeMajorStep();

        // Download Libraries
        progressContainer.setProgressText("Downloading Libraries");
        progressContainer.setNumMinorSteps(versionManifest.getAsJsonArray("libraries").size());

        File libsDir = new File(versionFolder.getPath(), "libraries");
        libsDir.mkdirs();
        File nativesDir = new File(versionFolder.getPath(), "natives");
        nativesDir.mkdirs();
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

        // Update Versions Manifest
        progressContainer.setProgressText("Updating Versions Manifest");

        VersionManifest manifest = Manifests.VERSION;
        manifest.addLibs(version, files);
        manifest.saveAsync();

        progressContainer.completeMajorStep();
    }

    private Optional<File> installLibrary(JsonObject library, File libDir, File nativeDir, ProgressContainer progress) throws IOException {
        File f = null;
        // Check if the library has conditions
        if (library.has("rules")) {
            for (JsonElement ruleElement : library.getAsJsonArray("rules")) {
                final JsonObject rule = ruleElement.getAsJsonObject();
                final String action = rule.get("action").getAsString();

                // Ensure the rule is valid
                if (!rule.has("os")) continue;
                final String os = rule.getAsJsonObject("os").get("name").getAsString();

                // If the user isn't using the OS the rule is for, skip it.
                if (action.equals("allow") && isUserOs(os)) continue;
                if (!(action.equals("dissallow") && isUserOs(os))) continue;

                progress.completeMinorStep();
                Logger.INSTANCE.debug("Skipping library: " + library.get("name").getAsString());
                return Optional.empty();
            }
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

    private static boolean isUserOs(String os){
        Logger.INSTANCE.debug("Is Mac: " + SystemUtils.IS_OS_MAC);
        switch(os) {
            case "windows": return SystemUtils.IS_OS_WINDOWS;
            case "osx": return SystemUtils.IS_OS_MAC;
            case "linux": return SystemUtils.IS_OS_LINUX;
            default: {
                Logger.INSTANCE.info("Tried checking for OS:" + os + ". Did not match Pattern (win/osx/linux).");
                return false;
            }
        }
    }

    @Override
    public InstallationStages getRequiredStage() {
        return InstallationStages.INSTALL;
    }
}