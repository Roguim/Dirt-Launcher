package net.dirtcraft.dirtlauncher.game.installation.tasks.installation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.configuration.ConfigurationManager;
import net.dirtcraft.dirtlauncher.configuration.manifests.ForgeManifest;
import net.dirtcraft.dirtlauncher.configuration.manifests.VersionManifest;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.lib.data.json.forge.ForgeInstallManifest;
import net.dirtcraft.dirtlauncher.lib.data.json.forge.ForgePostProcess;
import net.dirtcraft.dirtlauncher.lib.data.json.mojang.Library;
import net.dirtcraft.dirtlauncher.lib.data.tasks.*;
import net.dirtcraft.dirtlauncher.lib.util.Jar;
import net.dirtcraft.dirtlauncher.logging.Logger;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
    public void executeTask(ProgressContainer progressContainer, ConfigurationManager config) throws IOException {
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
        DownloadTask forgeDl = new DownloadTask(MiscUtils.getURL(url).orElse(null), forgeInstaller);
        TaskExecutor.execute(Collections.singleton(forgeDl), progressContainer.showBitrate(), "Downloading Forge");
        progressContainer.completeMinorStep();
        progressContainer.nextMajorStep();

        // Extract the Forge Installer & Write the Forge JSON manifest
        progressContainer.setProgressText("Extracting Forge Installer");
        progressContainer.setNumMinorSteps(2);


        Jar installerJar = new Jar(forgeInstaller);
        JsonTask<ForgeInstallManifest> profileTask = new JsonTask<>(installerJar, "install_profile.json", ForgeInstallManifest.class);
        ForgeInstallManifest installManifest = profileTask.runUnchecked();
        progressContainer.setNumMinorSteps(1);

        // Install Forge universal jar on newer versions of Forge because it does not become packed in the installer jar

        ExtractTask versionTask = new ExtractTask(installerJar, "version.json", entry.getForgeManifestFile());
        versionTask.runUnchecked();
        JsonObject forgeVersionManifest = JsonUtils.readJsonFromFile(entry.getForgeManifestFile());
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
            DownloadTask forge = new DownloadTask(new URL(forgeUrl), entry.getForgeJarFile());
            TaskExecutor.execute(Collections.singleton(forge), progressContainer.showBitrate(), "Downloading Forge");
        }
        List<File> libraries = Collections.synchronizedList(new ArrayList<>());
        progressContainer.setNumMinorSteps(librariesArray.size());

        List<FileTask> downloads = StreamSupport.stream(librariesArray.spliterator(), false)
                .map(JsonElement::getAsJsonObject)
                .map(lib->getLibraryDownload(lib, forgeFolder, installerJar))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        Collection<FileTask> baseDownloads = TaskExecutor.execute(downloads, progressContainer.showBitrate(), "Downloading Libraries");

        downloads = baseDownloads.stream()
                .filter(Task::completedExceptionally)
                .map(DownloadTask.class::cast)
                .map(this::tryAsXZ)
                .collect(Collectors.toList());

        Collection<FileTask> packedDownloads = TaskExecutor.execute(downloads, progressContainer.showBitrate(), "Downloading Additional Libraries");

        {
            Task<?> exception = packedDownloads.stream().filter(Task::completedExceptionally).findFirst().orElse(null);
            if (exception != null) exception.throwException();
        }

        Optional<IOException> exception = packedDownloads.stream()
                .map(this::unpackXZ)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();

        if (exception.isPresent()) throw exception.get();
        baseDownloads.removeIf(Task::completedExceptionally);
        baseDownloads.addAll(packedDownloads);
        baseDownloads.forEach(lib->addToLaunchCode(lib, libraries));

        run(progressContainer, installManifest, new File(forgeFolder, "libraries"), installerJar, config);
        forgeInstaller.delete();

        // Update Forge Versions Manifest
        progressContainer.setProgressText("Updating Forge Versions Manifest");
        File tempForgeJar = new File(tempDir, entry.getForgeJarFilename());
        if (tempForgeJar.exists()) tempForgeJar.renameTo(entry.getForgeJarFile());
        FileUtils.deleteDirectoryUnchecked(entry.getTempFolder().toFile());
        entry.addLibs(libraries);
        entry.saveAsync();

        progressContainer.nextMajorStep();
    }

    private Optional<FileTask> getLibraryDownload(JsonObject library, File forgeFolder, ZipFile installer) {
        String[] libraryMaven = library.get("name").getAsString().split(":");

        // We already installed forge, no need to do it again.
        //if (libraryMaven[1].equals("forge")) {
        //    Logger.INSTANCE.debug("Skipping forge library because Forge is already installed!");
        //    return Optional.empty();
        //}
        Logger.INSTANCE.debug("Installing library: " + libraryMaven[1]);

        // Establish paths
        File libraryPath = forgeFolder.toPath()
                .resolve("libraries")
                .resolve(libraryMaven[0].replace(".", File.separator))
                .resolve(libraryMaven[1])
                .resolve(libraryMaven[2])
                .toFile();
        libraryPath.mkdirs();
        String url = JsonUtils.getJsonElement(library, "downloads", "artifact", "url")
                .filter(j->!j.isJsonNull())
                .map(JsonElement::getAsString)
                .filter(s->!s.isEmpty())
                .orElse(null);
        String path = JsonUtils.getJsonElement(library, "downloads", "artifact", "path")
                .filter(j->!j.isJsonNull())
                .map(JsonElement::getAsString)
                .filter(s->!s.isEmpty())
                .orElse(null);
        File libraryFile = new File(libraryPath, String.format("%s-%s.jar", libraryMaven[1], libraryMaven[2]));
        if (url == null && path != null) {
            ZipEntry entry = installer.getEntry(String.format("maven/%s", path));
            if (entry != null) return Optional.of(new ExtractTask(installer, entry, libraryFile));
        }
        url = parseUrlElement(url, library, libraryMaven).replace("http://", "https://");

        // Install the library
        Logger.INSTANCE.debug("Downloading " + url);
        return Optional.of(new DownloadTask(MiscUtils.getURL(url).orElseThrow(NullPointerException::new), libraryFile));
    }

    private String parseUrlElement(String urlElement, JsonObject library, String[] libraryMaven) {
        if (urlElement != null) return urlElement;
        final String concatLibrary = String.format("%s/%s/%s/%s-%s.jar", libraryMaven[0].replace(".", "/"), libraryMaven[1], libraryMaven[2], libraryMaven[1], libraryMaven[2]);
        return library.has("url") ? library.get("url").getAsString() + concatLibrary : "https://libraries.minecraft.net/" + concatLibrary;
    }

    private DownloadTask tryAsXZ(DownloadTask result){
        final File fileXZ = new File(result.destination.toString() + ".pack.xz");
        final URL urlXZ = MiscUtils.getURL(result.getSrc().toString() + ".pack.xz").orElse(null);
        return new DownloadTask(urlXZ, fileXZ);
    }

    private Optional<IOException> unpackXZ(FileTask result) {
        try {
            FileUtils.unpackPackXZ(result.getResult());
            return Optional.empty();
        } catch (IOException e){
            return Optional.of(e);
        }
    }

    private void addToLaunchCode(FileTask result, List<File> librariesLaunchCode){
        String lib = StringUtils.substringBeforeLast(result.getResult().getPath(), ".pack.xz");
        librariesLaunchCode.add(new File(lib));
    }


    private void run(ProgressContainer container, ForgeInstallManifest manifest, File libraryDir, Jar installerJar, ConfigurationManager config) {
        List<FileTask> downloads = new ArrayList<>();
        for (Library lib : manifest.getLibraries()) lib.getArtifact()
                .map(d->{
                    if (d.getUrl() != null) return d.getDownload(libraryDir);
                    else return new ExtractTask(installerJar, "maven/" + d.path, libraryDir.toPath().resolve(d.path).toFile());
                }).ifPresent(downloads::add);
        TaskExecutor.execute(downloads, container.showBitrate());
        ForgePostProcess p = manifest.getPostProcess();
        container.nextMajorStep("Applying post-processors");
        p.process(libraryDir, config.getVersionManifest().get(manifest.getMinecraft()).map(VersionManifest.Entry::getVersionJarFile).get(), installerJar);
    }


    @Override
    public InstallationStages getRequiredStage() {
        return InstallationStages.INSTALL;
    }
}
