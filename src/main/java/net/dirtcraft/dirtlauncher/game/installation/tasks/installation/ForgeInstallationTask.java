package net.dirtcraft.dirtlauncher.game.installation.tasks.installation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.Data.Config;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.gui.home.sidebar.Pack;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.StreamSupport;

public class ForgeInstallationTask implements IInstallationTask {

    private final Pack pack;

    public ForgeInstallationTask(Pack pack) {
        this.pack = pack;
    }

    public int getNumberSteps() {
        return 3;
    }

    @Override
    public void executeTask(ExecutorService threadService, ProgressContainer progressContainer, Config config) throws IOException {
        // Update Progress
        progressContainer.setProgressText("Downloading Forge Installer");
        progressContainer.setNumMinorSteps(2);

        // Prepare the Forge folder
        File forgeFolder = new File(config.getForgeDirectory(), pack.getForgeVersion());
        FileUtils.deleteDirectory(forgeFolder);
        forgeFolder.mkdirs();
        progressContainer.completeMinorStep();

        // Download the Forge installer
        File forgeInstaller = new File(forgeFolder, "installer.jar");
        // 1.7 did some strange stuff with forge file names
        String url = pack.getGameVersion().equals("1.7.10")
                ? String.format("https://files.minecraftforge.net/maven/net/minecraftforge/forge/%s-%s-%s/forge-%s-%s-%s-installer.jar", pack.getGameVersion(), pack.getForgeVersion(), pack.getGameVersion(), pack.getGameVersion(), pack.getForgeVersion(), pack.getGameVersion())
                : String.format("https://files.minecraftforge.net/maven/net/minecraftforge/forge/%s-%s/forge-%s-%s-installer.jar", pack.getGameVersion(), pack.getForgeVersion(), pack.getGameVersion(), pack.getForgeVersion());
        FileUtils.copyURLToFile(url, forgeInstaller);
        progressContainer.completeMinorStep();
        progressContainer.completeMajorStep();

        // Extract the Forge Installer & Write the Forge JSON manifest
        progressContainer.setProgressText("Extracting Forge Installer");
        progressContainer.setNumMinorSteps(2);

        JsonObject forgeVersionManifest = FileUtils.extractForgeJar(forgeInstaller, forgeFolder.getPath());
        forgeInstaller.delete();
        progressContainer.setNumMinorSteps(1);

        FileUtils.writeJsonToFile(new File(forgeFolder, pack.getForgeVersion() + ".json"), forgeVersionManifest);
        progressContainer.completeMinorStep();
        progressContainer.completeMajorStep();

        // Download the Forge Libraries
        progressContainer.setProgressText("Downloading Forge Libraries");
        JsonArray librariesArray = forgeVersionManifest.getAsJsonObject("versionInfo").getAsJsonArray("libraries");
        StringBuffer librariesLaunchCode = new StringBuffer();
        progressContainer.setNumMinorSteps(librariesArray.size());

        try {
            CompletableFuture.allOf(
                    StreamSupport.stream(librariesArray.spliterator(), false)
                        .map(JsonElement::getAsJsonObject)
                        .map(library -> CompletableFuture.runAsync(() -> {
                            try {
                                installLibrary(library, forgeFolder, librariesLaunchCode, progressContainer);
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

        // Update Forge Versions Manifest
        progressContainer.setProgressText("Updating Forge Versions Manifest");

        JsonObject forgeVersionJsonObject = new JsonObject();
        forgeVersionJsonObject.addProperty("version", pack.getForgeVersion());
        forgeVersionJsonObject.addProperty("classpathLibraries", StringUtils.substringBeforeLast(forgeFolder + File.separator + "forge-" + pack.getGameVersion() + "-" + pack.getForgeVersion() + "-universal.jar;" + librariesLaunchCode.toString(), ";"));

        File forgeVersionsManifestFile = config.getDirectoryManifest(config.getForgeDirectory());
        JsonObject forgeManifest = FileUtils.readJsonFromFile(forgeVersionsManifestFile);
        forgeManifest.getAsJsonArray("forgeVersions").add(forgeVersionJsonObject);
        FileUtils.writeJsonToFile(forgeVersionsManifestFile, forgeManifest);

        progressContainer.completeMajorStep();
    }

    private void installLibrary(JsonObject library, File forgeFolder, StringBuffer librariesLaunchCode, ProgressContainer progressContainer) throws IOException {
        String[] libraryMaven = library.get("name").getAsString().split(":");

        // We already installed forge, no need to do it again.
        if (libraryMaven[1].equals("forge")) {
            progressContainer.completeMinorStep();
            return;
        }

        // Establish paths
        File libraryPath = new File(forgeFolder + File.separator + "libraries" + File.separator + libraryMaven[0].replace(".", File.separator) + File.separator + libraryMaven[1] + File.separator + libraryMaven[2]);
        libraryPath.mkdirs();
        String url = library.has("url")
                ? library.get("url").getAsString()
                : "https://libraries.minecraft.net/";
        url += String.format("%s/%s/%s/%s-%s.jar", libraryMaven[0].replace(".", "/"), libraryMaven[1], libraryMaven[2], libraryMaven[1], libraryMaven[2]);

        String fileName = String.format("%s%s%s-%s.jar", libraryPath, File.separator, libraryMaven[1], libraryMaven[2]);

        // Typesafe does some weird stuff
        if (libraryMaven[0].contains("typesafe")) {
            url += ".pack.xz";
            fileName += ".pack.xz";
        }

        // Install the library
        File libraryFile = new File(fileName);
        FileUtils.copyURLToFile(url, libraryFile);
        if (libraryFile.getName().contains(".pack.xz")) FileUtils.unpackPackXZ(libraryFile);
        librariesLaunchCode.append(StringUtils.substringBeforeLast(libraryFile.getPath(), ".pack.xz") + ";");

        progressContainer.completeMinorStep();
    }
}
