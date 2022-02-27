package net.dirtcraft.dirtlauncher.game.installation.tasks.installation;

import net.dirtcraft.dirtlauncher.configuration.ConfigurationManager;
import net.dirtcraft.dirtlauncher.configuration.manifests.AssetManifest;
import net.dirtcraft.dirtlauncher.data.Minecraft.Asset;
import net.dirtcraft.dirtlauncher.data.Minecraft.GameAssetManifest;
import net.dirtcraft.dirtlauncher.data.Minecraft.GameVersion;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.DownloadManager;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.DownloadMeta;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.IFileDownload;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.progress.Trackers;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;
import net.dirtcraft.dirtlauncher.utils.WebUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AssetsInstallationTask implements IInstallationTask {

    private final GameVersion versionManifest;

    public AssetsInstallationTask(GameVersion versionManifest) {
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
        String gameVersion = versionManifest.getId();
        String assetVersion =  versionManifest.getAssets();
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

        String assetsUrl = versionManifest.getAssetIndex().getUrl().toString();
        File indexes = new File(assetsFolder, "indexes");
        indexes.mkdirs();
        GameAssetManifest assetMap = WebUtils.getGsonFromUrl(assetsUrl, GameAssetManifest.class)
                .orElseThrow(IOException::new);
        JsonUtils.toJson(new File(indexes, versionManifest.getAssets() + ".json"), assetMap, GameAssetManifest.class);
        progressContainer.completeMinorStep();

        // Download Assets
        Collection<Asset> assets = assetMap.getObjects().values();
        progressContainer.setNumMinorSteps(assets.size());

        List<IFileDownload> downloads = assets.stream().map(asset-> getLibDownload(asset, assetsFolder)).collect(Collectors.toList());
        Trackers.MultiUpdater tracker = Trackers.getTracker(progressContainer, "Fetching Downloads", "Downloading Assets");
        downloadManager.download(tracker, downloads);

        // Update Assets Manifest
        progressContainer.setProgressText("Updating Assets Manifest");
        manifest.add(gameVersion, assetVersion);
        manifest.saveAsync();
        progressContainer.nextMajorStep();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private DownloadMeta getLibDownload(Asset value, File assetsFolder) {
        String firstTwo = value.getHash().substring(0, 2);
        File assetFolder = new File(new File(assetsFolder, "objects"), firstTwo);
        assetFolder.mkdirs();
        final URL asset = MiscUtils.getURL("http://resources.download.minecraft.net/" + firstTwo + "/" + value.getHash()).orElse(null);
        final File dest = new File(assetFolder.getPath(), value.getHash());
        return new DownloadMeta(asset, dest, value.getSize());
    }

    @Override
    public InstallationStages getRequiredStage() {
        return InstallationStages.INSTALL;
    }
}
