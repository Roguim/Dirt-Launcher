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
import net.dirtcraft.dirtlauncher.lib.data.json.forge.ForgeVersion;
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
        return 5;
    }

    @Override
    public void executeTask(ProgressContainer progressContainer, ConfigurationManager config) throws IOException {
        // Prepare the Forge folder
        ForgeManifest manifest = config.getForgeManifest();
        ForgeManifest.Entry entry = manifest.create(pack.getGameVersion(), pack.getForgeVersion());
        File tempDir = entry.getTempFolder().toFile();
        File forgeFolder = entry.getForgeFolder().toFile();

        // Download the Forge installer
        File forgeInstaller = new File(tempDir, "installer.jar");
        // 1.7 did some strange stuff with forge file names
        String url = pack.getGameVersion().equals("1.7.10")
                ? String.format("https://files.minecraftforge.net/maven/net/minecraftforge/forge/%s-%s-%s/forge-%s-%s-%s-installer.jar", pack.getGameVersion(), pack.getForgeVersion(), pack.getGameVersion(), pack.getGameVersion(), pack.getForgeVersion(), pack.getGameVersion())
                : String.format("https://files.minecraftforge.net/maven/net/minecraftforge/forge/%s-%s/forge-%s-%s-installer.jar", pack.getGameVersion(), pack.getForgeVersion(), pack.getGameVersion(), pack.getForgeVersion());
        DownloadTask forgeDl = new DownloadTask(MiscUtils.getURL(url).orElse(null), forgeInstaller);
        TaskExecutor.execute(Collections.singleton(forgeDl), progressContainer.showBitrate(), "Downloading Forge");

        Jar installerJar = new Jar(forgeInstaller);

        ForgeVersion version = ForgeVersion.fromInstaller(pack.getGameVersion(), installerJar).run();

        JsonTask<ForgeInstallManifest> profileTask = new JsonTask<>(installerJar, "install_profile.json", ForgeInstallManifest.class);
        ForgeInstallManifest installManifest = profileTask.run();

        // Install Forge universal jar on newer versions of Forge because it does not become packed in the installer jar

        ExtractTask versionTask = new ExtractTask(installerJar, "version.json", entry.getForgeManifestFile());
        versionTask.run();
        JsonObject forgeVersionManifest = JsonUtils.readJsonFromFile(entry.getForgeManifestFile());
        JsonUtils.writeJsonToFile(entry.getForgeManifestFile(), forgeVersionManifest);

        // Download the Forge Libraries

        List<File> libraries = Collections.synchronizedList(new ArrayList<>());

        List<FileTask> downloads = version.getClientLibraries(new File(forgeFolder, "libraries"), installerJar)
                .collect(TaskExecutor.collector(progressContainer.showBitrate(), "Downloading Libraries"))
                .stream()
                .peek(dl->{if (!dl.completedExceptionally()) addToLaunchCode(dl, libraries);})
                .filter(Task::completedExceptionally)
                .map(DownloadTask.class::cast)
                .map(this::tryAsXZ)
                .collect(TaskExecutor.collector(progressContainer.showBitrate(), "Downloading Compressed Libraries"));

        {
            Task<?> exception = downloads.stream().filter(Task::completedExceptionally).findFirst().orElse(null);
            if (exception != null) exception.throwException();
        }

        downloads.forEach(lib->{
            unpackXZ(lib);
            addToLaunchCode(lib, libraries);
        });

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
        TaskExecutor.execute(downloads, container.showBitrate(), "Downloading Additional Libraries");
        ForgePostProcess p = manifest.getClientPostProcess();
        container.nextMajorStep("Applying post-processors");
        p.process(libraryDir, config.getVersionManifest().get(manifest.getMinecraft()).map(VersionManifest.Entry::getVersionJarFile).get(), installerJar);
    }


    @Override
    public InstallationStages getRequiredStage() {
        return InstallationStages.INSTALL;
    }
}
