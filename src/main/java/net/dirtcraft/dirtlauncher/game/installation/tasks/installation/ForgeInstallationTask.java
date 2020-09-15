package net.dirtcraft.dirtlauncher.game.installation.tasks.installation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.configuration.Config;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.utils.Constants;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;
import net.dirtcraft.dirtlauncher.utils.WebUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.StreamSupport;

public class ForgeInstallationTask implements IInstallationTask {

    private final Modpack pack;

    public ForgeInstallationTask(Modpack pack) {
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
        WebUtils.copyURLToFile(url, forgeInstaller);
        progressContainer.completeMinorStep();
        progressContainer.completeMajorStep();

        // Extract the Forge Installer & Write the Forge JSON manifest
        progressContainer.setProgressText("Extracting Forge Installer");
        progressContainer.setNumMinorSteps(2);

        JsonObject forgeVersionManifest = FileUtils.extractForgeJar(forgeInstaller, forgeFolder);
        forgeInstaller.delete();
        progressContainer.setNumMinorSteps(1);

        // Install Forge universal jar on newer versions of Forge because it does not become packed in the installer jar

        JsonUtils.writeJsonToFile(new File(forgeFolder, pack.getForgeVersion() + ".json"), forgeVersionManifest);
        progressContainer.completeMinorStep();
        progressContainer.completeMajorStep();

        // Download the Forge Libraries
        progressContainer.setProgressText("Downloading Forge Libraries");
        JsonArray librariesArray;
        if (forgeVersionManifest.has("versionInfo")) librariesArray = forgeVersionManifest.getAsJsonObject("versionInfo").getAsJsonArray("libraries");
        else {
            librariesArray = forgeVersionManifest.getAsJsonArray("libraries");
            WebUtils.copyURLToFile(
                    String.format("https://files.minecraftforge.net/maven/net/minecraftforge/forge/%s-%s/forge-%s-%s-universal.jar", pack.getGameVersion(), pack.getForgeVersion(), pack.getGameVersion(), pack.getForgeVersion()),
                    new File(forgeFolder + File.separator + "forge-" + pack.getGameVersion() + "-" + pack.getForgeVersion() + "-universal.jar"));
        }
        StringBuffer librariesLaunchCode = new StringBuffer();
        progressContainer.setNumMinorSteps(librariesArray.size());

        try {
            CompletableFuture.allOf(
                    StreamSupport.stream(librariesArray.spliterator(), false)
                        .map(JsonElement::getAsJsonObject)
                        .map(library -> CompletableFuture.runAsync(() -> {
                            try {
                                installLibrary(library, forgeFolder, librariesLaunchCode, progressContainer);
                            } catch (Throwable e) {
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
        JsonObject forgeManifest = JsonUtils.readJsonFromFile(forgeVersionsManifestFile);
        forgeManifest.getAsJsonArray("forgeVersions").add(forgeVersionJsonObject);
        JsonUtils.writeJsonToFile(forgeVersionsManifestFile, forgeManifest);

        progressContainer.completeMajorStep();
    }

    private void installLibrary(JsonObject library, File forgeFolder, StringBuffer librariesLaunchCode, ProgressContainer progressContainer) throws IOException {
        String[] libraryMaven = library.get("name").getAsString().split(":");

        // We already installed forge, no need to do it again.
        if (libraryMaven[1].equals("forge")) {
            if (Constants.DEBUG) System.out.println("Skipping forge library because Forge is already installed!");
            progressContainer.completeMinorStep();
            return;
        }
        if (Constants.DEBUG) System.out.println("Installing library: " + libraryMaven[1]);

        // Establish paths
        File libraryPath = new File(forgeFolder + File.separator + "libraries" + File.separator + libraryMaven[0].replace(".", File.separator) + File.separator + libraryMaven[1] + File.separator + libraryMaven[2]);
        libraryPath.mkdirs();
        String url;
        JsonElement urlElement = JsonUtils.getJsonElement(library, "downloads", "artifact", "url").orElse(JsonNull.INSTANCE);
        if (!urlElement.isJsonNull()) url = urlElement.getAsString();
        else {
            url = library.has("url")
                    ? library.get("url").getAsString()
                    : "https://libraries.minecraft.net/";
            url += String.format("%s/%s/%s/%s-%s.jar", libraryMaven[0].replace(".", "/"), libraryMaven[1], libraryMaven[2], libraryMaven[1], libraryMaven[2]);
        }

        String fileName = String.format("%s%s%s-%s.jar", libraryPath, File.separator, libraryMaven[1], libraryMaven[2]);

        // Install the library
        File libraryFile;
        try {
            libraryFile = new File(fileName);
            if (Constants.DEBUG) System.out.println("Downloading " + url);
            WebUtils.copyURLToFile(url, libraryFile);
        } catch (Exception e){
            try {Thread.sleep(2000);} catch(InterruptedException ex) {}
            // Typesafe does some weird stuff
            if (library.has("downloads")) e.printStackTrace();
            else {
                url += ".pack.xz";
                fileName += ".pack.xz";
            }
            libraryFile = new File(fileName);
            WebUtils.copyURLToFile(url, libraryFile);
        }
        if (libraryFile.getName().contains(".pack.xz")) FileUtils.unpackPackXZ(libraryFile);
        librariesLaunchCode.append(StringUtils.substringBeforeLast(libraryFile.getPath(), ".pack.xz") + ";");

        progressContainer.completeMinorStep();
    }

    @Override
    public InstallationStages getRequiredStage() {
        return InstallationStages.INSTALL;
    }
}
