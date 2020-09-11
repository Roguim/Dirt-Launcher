package net.dirtcraft.dirtlauncher.game.installation.tasks.installation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.Data.Config;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
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
        FileUtils.writeJsonToFile(versionFolder.toPath().resolve(version + ".json").toFile(), versionManifest);
        progressContainer.completeMinorStep();

        // Download jar
        FileUtils.copyURLToFile(versionManifest.getAsJsonObject("downloads").getAsJsonObject("client").get("url").getAsString(), new File(versionFolder.getPath(), version + ".jar"));
        progressContainer.completeMinorStep();
        progressContainer.completeMajorStep();

        // Download Libraries
        progressContainer.setProgressText("Downloading Libraries");
        progressContainer.setNumMinorSteps(versionManifest.getAsJsonArray("libraries").size());

        File libsDir = new File(versionFolder.getPath(), "libraries");
        libsDir.mkdirs();
        File nativesDir = new File(versionFolder.getPath(), "natives");
        nativesDir.mkdirs();
        StringBuffer launchPaths = new StringBuffer();

        try {
            CompletableFuture.allOf(
                    StreamSupport.stream(versionManifest.getAsJsonArray("libraries").spliterator(), false)
                        .map(JsonElement::getAsJsonObject)
                        .map(library -> CompletableFuture.runAsync(() -> {
                            try {
                                installLibrary(library, launchPaths, libsDir, nativesDir, progressContainer);
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

        JsonObject versionJsonObject = new JsonObject();
        versionJsonObject.addProperty("version", version);
        versionJsonObject.addProperty("classpathLibraries", StringUtils.substringBeforeLast(launchPaths.toString(), ";"));

        File versionsManifestFile = config.getDirectoryManifest(config.getVersionsDirectory());
        JsonObject versionsManifest = FileUtils.readJsonFromFile(versionsManifestFile);
        versionsManifest.getAsJsonArray("versions").add(versionJsonObject);
        FileUtils.writeJsonToFile(versionsManifestFile, versionsManifest);

        progressContainer.completeMajorStep();
    }

    private void installLibrary(JsonObject library, StringBuffer launchPaths, File libDir, File nativeDir, ProgressContainer progress) throws IOException {
        // Check if the library has conditions
        if (library.has("rules")) {
            for (JsonElement ruleElement : library.getAsJsonArray("rules")) {
                final JsonObject rule = ruleElement.getAsJsonObject();
                final String action = rule.get("action").getAsString();

                // Ensure the rule is valid
                if (!(rule.has("os") && (action.equals("allow") || action.equals("disallow")))) continue;
                final String os = rule.getAsJsonObject("os").get("name").getAsString();

                // If the user isn't using the OS the rule is for, skip it.
                if (!isUserOs(os).map(b -> b == (action.equals("allow"))).orElse(true)) continue;
                progress.completeMinorStep();
                return;
            }
        }

        JsonObject libraryDownloads = library.getAsJsonObject("downloads");

        // Download any standard libraries
        if (libraryDownloads.has("artifact")) {
            new File(libDir, StringUtils.substringBeforeLast(libraryDownloads.getAsJsonObject("artifact").get("path").getAsString(), "/").replace("/", File.separator)).mkdirs();
            String filePath = libDir.getPath() + File.separator + libraryDownloads.getAsJsonObject("artifact").get("path").getAsString().replace("/", File.separator);
            FileUtils.copyURLToFile(libraryDownloads.getAsJsonObject("artifact").get("url").getAsString(), new File(filePath));
            launchPaths.append(filePath + ";");
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
                FileUtils.copyURLToFile(nativeJson.get("url").getAsString(), outputFile);
                FileUtils.extractJar(outputFile.getPath(), nativeDir.getPath());
                outputFile.delete();
            }
        }

        progress.completeMinorStep();
    }

    private static Optional<Boolean> isUserOs(String os){
        switch(os) {
            case "windows": return Optional.of(SystemUtils.IS_OS_WINDOWS);
            case "osx": return Optional.of(SystemUtils.IS_OS_MAC);
            case "linux": return Optional.of(SystemUtils.IS_OS_LINUX);
            default: {
                System.out.println("Tried checking for OS:" + os + ". Did not match Pattern (win/osx/linux).");
                return Optional.empty();
            }
        }
    }
}
