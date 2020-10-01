package net.dirtcraft.dirtlauncher.game.installation.tasks.installation;

import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.configuration.ConfigurationManager;
import net.dirtcraft.dirtlauncher.configuration.manifests.AssetManifest;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.DownloadManager;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;
import net.dirtcraft.dirtlauncher.utils.WebUtils;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

public class AssetsInstallationTask implements IInstallationTask {

    private final JsonObject versionManifest;

    public AssetsInstallationTask(JsonObject versionManifest) {
        this.versionManifest = versionManifest;
    }

    public int getNumberSteps() {
        return 1;
    }

    @Override
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
            progressContainer.completeMajorStep();
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

        try {
            CompletableFuture.allOf(
                    assetKeys.stream()
                        .map(asset -> CompletableFuture.runAsync(() -> {
                            try {
                                installAsset(assetsManifest, asset, assetsFolder, progressContainer);
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

        // Update Assets Manifest
        progressContainer.setProgressText("Updating Assets Manifest");
        manifest.add(gameVersion, assetVersion);
        manifest.saveAsync();
        progressContainer.completeMajorStep();
    }

    private void installAsset(JsonObject assetsManifest, String assetKey, File assetsFolder, ProgressContainer progressContainer) throws IOException {
        String hash = assetsManifest.getAsJsonObject("objects").getAsJsonObject(assetKey).get("hash").getAsString();
        File assetFolder = new File(new File(assetsFolder, "objects"), hash.substring(0, 2));
        assetFolder.mkdirs();
        WebUtils.copyURLToFile("http://resources.download.minecraft.net/" + hash.substring(0, 2) + "/" + hash, new File(assetFolder.getPath(), hash));
        progressContainer.completeMinorStep();
    }

    @Override
    public InstallationStages getRequiredStage() {
        return InstallationStages.INSTALL;
    }
}
