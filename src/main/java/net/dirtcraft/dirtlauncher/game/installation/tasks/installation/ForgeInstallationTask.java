package net.dirtcraft.dirtlauncher.game.installation.tasks.installation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.configuration.ConfigurationManager;
import net.dirtcraft.dirtlauncher.configuration.manifests.ForgeManifest;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.DownloadManager;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.DownloadMeta;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.IFileDownload;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.Result;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.progress.Trackers;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.logging.Logger;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class ForgeInstallationTask implements IInstallationTask {

    private final Modpack pack;

    public ForgeInstallationTask(Modpack pack) {
        this.pack = pack;
    }

    public int getNumberSteps() {
        return 3;
    }

    @Override
    public void executeTask(DownloadManager  downloadManager, ProgressContainer progressContainer, ConfigurationManager config) throws IOException {
        // Update Progress
        progressContainer.setProgressText("Downloading Forge Installer");
        progressContainer.setNumMinorSteps(2);

        // Prepare the Forge folder
        ForgeManifest manifest = config.getForgeManifest();
        ForgeManifest.Entry entry = manifest.create(pack.getGameVersion(), pack.getForgeVersion());
        File tempDir = entry.getTempFolder().toFile();
        File forgeFolder = entry.getForgeFolder().toFile();
        progressContainer.completeMinorStep();

        // Download the Forge installer
        File forgeInstaller = new File(tempDir, "installer.jar");
        // 1.7 did some strange stuff with forge file names
        String url = pack.getGameVersion().equals("1.7.10")
                ? String.format("https://files.minecraftforge.net/maven/net/minecraftforge/forge/%s-%s-%s/forge-%s-%s-%s-installer.jar", pack.getGameVersion(), pack.getForgeVersion(), pack.getGameVersion(), pack.getGameVersion(), pack.getForgeVersion(), pack.getGameVersion())
                : String.format("https://files.minecraftforge.net/maven/net/minecraftforge/forge/%s-%s/forge-%s-%s-installer.jar", pack.getGameVersion(), pack.getForgeVersion(), pack.getGameVersion(), pack.getForgeVersion());
        DownloadMeta forgeDl = new DownloadMeta(MiscUtils.getURL(url).orElse(null), forgeInstaller);
        Trackers.MultiUpdater tracker = Trackers.getTracker(progressContainer, "Fetching Download", "Downloading Forge");
        downloadManager.download(tracker, forgeDl);
        progressContainer.completeMinorStep();
        progressContainer.nextMajorStep();

        // Extract the Forge Installer & Write the Forge JSON manifest
        progressContainer.setProgressText("Extracting Forge Installer");
        progressContainer.setNumMinorSteps(2);

        JsonObject forgeVersionManifest = FileUtils.extractForgeJar(forgeInstaller, tempDir); //todo some sort of progress display?
        forgeInstaller.delete();
        progressContainer.setNumMinorSteps(1);

        // Install Forge universal jar on newer versions of Forge because it does not become packed in the installer jar

        JsonUtils.writeJsonToFile(entry.getForgeManifestFile(), forgeVersionManifest);
        progressContainer.completeMinorStep();
        progressContainer.nextMajorStep();

        // Download the Forge Libraries
        progressContainer.setProgressText("Downloading Forge Libraries");
        JsonArray librariesArray;
        if (forgeVersionManifest.has("versionInfo")) librariesArray = forgeVersionManifest.getAsJsonObject("versionInfo").getAsJsonArray("libraries");
        else {
            librariesArray = forgeVersionManifest.getAsJsonArray("libraries");
            String forgeUrl = String.format("https://files.minecraftforge.net/maven/net/minecraftforge/forge/%s-%s/forge-%s-%s-universal.jar", pack.getGameVersion(), pack.getForgeVersion(), pack.getGameVersion(), pack.getForgeVersion());
            DownloadMeta forge = new DownloadMeta(forgeUrl, entry.getForgeJarFile());
            Trackers.MultiUpdater forgeTracker = Trackers.getTracker(progressContainer, "Fetching Forge", "Downloading Forge");
            downloadManager.download(forgeTracker, forge);
        }
        List<File> libraries = Collections.synchronizedList(new ArrayList<>());
        progressContainer.setNumMinorSteps(librariesArray.size());

        List<IFileDownload> downloads = StreamSupport.stream(librariesArray.spliterator(), false)
                .map(JsonElement::getAsJsonObject)
                .map(lib->getLibraryDownload(lib, forgeFolder))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        Trackers.MultiUpdater updater = Trackers.getTracker(progressContainer, "Fetching Libraries", "Downloading Libraries");
        List<Result> baseDownloads = downloadManager.download(updater, downloads);

        downloads = baseDownloads.stream()
                .filter(Result::finishedExceptionally)
                .map(this::tryAsXZ)
                .collect(Collectors.toList());

        updater = Trackers.getTracker(progressContainer, "Fetching Additional Libraries", "Downloading Additional Libraries");
        List<Result> packedDownloads = downloadManager.download(updater, downloads);

        if (packedDownloads.stream().anyMatch(Result::finishedExceptionally)) throw new IOException();
        Optional<IOException> exception = packedDownloads.stream()
                .map(this::unpackXZ)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();

        if (exception.isPresent()) throw exception.get();
        baseDownloads.removeIf(Result::finishedExceptionally);
        baseDownloads.addAll(packedDownloads);
        baseDownloads.forEach(lib->addToLaunchCode(lib, libraries));


        // Update Forge Versions Manifest
        progressContainer.setProgressText("Updating Forge Versions Manifest");
        File tempForgeJar = new File(tempDir, entry.getForgeJarFilename());
        if (tempForgeJar.exists()) tempForgeJar.renameTo(entry.getForgeJarFile());
        FileUtils.deleteDirectoryUnchecked(entry.getTempFolder().toFile());
        entry.addLibs(libraries);
        entry.saveAsync();

        progressContainer.nextMajorStep();
    }

    private Optional<DownloadMeta> getLibraryDownload(JsonObject library, File forgeFolder) {
        String[] libraryMaven = library.get("name").getAsString().split(":");

        // We already installed forge, no need to do it again.
        if (libraryMaven[1].equals("forge")) {
            Logger.INSTANCE.debug("Skipping forge library because Forge is already installed!");
            return Optional.empty();
        }
        Logger.INSTANCE.debug("Installing library: " + libraryMaven[1]);

        // Establish paths
        File libraryPath = forgeFolder.toPath()
                .resolve("libraries")
                .resolve(libraryMaven[0].replace(".", File.separator))
                .resolve(libraryMaven[1])
                .resolve(libraryMaven[2])
                .toFile();
        libraryPath.mkdirs();
        JsonElement urlElement = JsonUtils.getJsonElement(library, "downloads", "artifact", "url").orElse(JsonNull.INSTANCE);
        String url = parseUrlElement(urlElement, library, libraryMaven);

        // Install the library
        File libraryFile = new File(String.format("%s%s%s-%s.jar", libraryPath, File.separator, libraryMaven[1], libraryMaven[2]));
        Logger.INSTANCE.debug("Downloading " + url);
        return Optional.of(new DownloadMeta(MiscUtils.getURL(url).orElse(null), libraryFile));
    }

    private String parseUrlElement(JsonElement urlElement, JsonObject library, String[] libraryMaven){
        if (!urlElement.isJsonNull()) return urlElement.getAsString();
        else return (library.has("url")? library.get("url").getAsString(): "https://libraries.minecraft.net/")
             + String.format("%s/%s/%s/%s-%s.jar", libraryMaven[0].replace(".", "/"), libraryMaven[1], libraryMaven[2], libraryMaven[1], libraryMaven[2]);
    }

    private DownloadMeta tryAsXZ(Result result){
        final File fileXZ = new File(result.getFile().toString() + ".pack.xz");
        final URL urlXZ = MiscUtils.getURL(result.getUrl().toString() + ".pack.xz").orElse(null);
        return new DownloadMeta(urlXZ, fileXZ);
    }

    private Optional<IOException> unpackXZ(Result result) {
        try {
            FileUtils.unpackPackXZ(result.getFile());
            return Optional.empty();
        } catch (IOException e){
            return Optional.of(e);
        }
    }

    private void addToLaunchCode(Result result, List<File> librariesLaunchCode){
        String lib = StringUtils.substringBeforeLast(result.getFile().getPath(), ".pack.xz");
        librariesLaunchCode.add(new File(lib));
    }

    @Override
    public InstallationStages getRequiredStage() {
        return InstallationStages.INSTALL;
    }
}
