package net.dirtcraft.dirtlauncher.game.installation.tasks.installation;

import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.configuration.ConfigurationManager;
import net.dirtcraft.dirtlauncher.configuration.manifests.AssetManifest;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.DownloadManager;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.DownloadMeta;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.IPresetDownload;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.progress.Trackers;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;
import net.dirtcraft.dirtlauncher.utils.WebUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AssetsInstallationTask implements IInstallationTask {

    private final JsonObject versionManifest;

    public AssetsInstallationTask(JsonObject versionManifest) {
        this.versionManifest = versionManifest;
    }

    public int getNumberSteps() {
        return 1;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void executeTask(DownloadManager downloadManager, ProgressContainer progressContainer, ConfigurationManager config) throws IOException {

        // Update Progress
        progressContainer.setProgressText("Downloading Assets");
        String gameVersion = versionManifest.get("id").getAsString();
        String assetVersion =  versionManifest.get("assets").getAsString();
        progressContainer.setNumMinorSteps(3);

        // Prepare the assets folder
        AssetManifest manifest = config.getAssetManifest();
        File assetsFolder = manifest.getDirectory().toFile();
        progressContainer.completeMinorStep();

        // Check if another game version shares the same assets which we are about to install:
        Optional<AssetManifest.Entry> optEntry = manifest.getViaAssetIndex(assetVersion);
        if (optEntry.isPresent()){
            manifest.add(gameVersion, optEntry.get());
            manifest.saveAsync();
            progressContainer.nextMajorStep();
            return;
        }

        // Write assets JSON manifest
        JsonObject assetsManifest = WebUtils.getJsonFromUrl(versionManifest.getAsJsonObject("assetIndex").get("url").getAsString());
        File indexes = new File(assetsFolder, "indexes");
        indexes.mkdirs();
        JsonUtils.writeJsonToFile(new File(indexes, versionManifest.get("assets").getAsString() + ".json"), assetsManifest);
        progressContainer.completeMinorStep();

        // Download Assets
        Set<String> assetKeys = assetsManifest.getAsJsonObject("objects").keySet();
        progressContainer.setNumMinorSteps(assetKeys.size());

        List<IPresetDownload> downloads = assetKeys.stream().map(asset-> getLibDownload(assetsManifest, asset, assetsFolder)).collect(Collectors.toList());
        Trackers.MultiUpdater tracker = Trackers.getTracker(progressContainer, "Fetching Downloads", "Downloading Assets");
        downloadManager.download(tracker, downloads);

        // Update Assets Manifest
        progressContainer.setProgressText("Updating Assets Manifest");
        manifest.add(gameVersion, assetVersion);
        manifest.saveAsync();
        progressContainer.nextMajorStep();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private DownloadMeta getLibDownload(JsonObject assetsManifest, String assetKey, File assetsFolder) {
        String hash = assetsManifest.getAsJsonObject("objects").getAsJsonObject(assetKey).get("hash").getAsString();
        long size = assetsManifest.getAsJsonObject("objects").getAsJsonObject(assetKey).get("size").getAsLong();
        File assetFolder = new File(new File(assetsFolder, "objects"), hash.substring(0, 2));
        assetFolder.mkdirs();
        final URL asset = MiscUtils.getURL("http://resources.download.minecraft.net/" + hash.substring(0, 2) + "/" + hash).orElse(null);
        final File dest = new File(assetFolder.getPath(), hash);
        return new DownloadMeta(asset, dest, size);
    }

    @Override
    public InstallationStages getRequiredStage() {
        return InstallationStages.INSTALL;
    }
}
