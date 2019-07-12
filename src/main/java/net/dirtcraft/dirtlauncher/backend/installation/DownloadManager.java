package net.dirtcraft.dirtlauncher.backend.installation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.scene.text.Text;
import net.dirtcraft.dirtlauncher.Controllers.Install;
import net.dirtcraft.dirtlauncher.backend.config.Paths;
import net.dirtcraft.dirtlauncher.backend.jsonutils.JsonFetcher;
import net.dirtcraft.dirtlauncher.backend.objects.OptionalMod;
import net.dirtcraft.dirtlauncher.backend.objects.Pack;
import net.dirtcraft.dirtlauncher.backend.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DownloadManager {

    public static void completePackSetup(Pack pack, List<OptionalMod> optionalMods) throws IOException  {
        JsonObject versionManifest = JsonFetcher.getVersionManifestJson(pack.getGameVersion());

        boolean installMinecraft = true;
        boolean installAssets = true;
        boolean installForge = true;
        boolean installPack = true;

        for(JsonElement jsonElement : FileUtils.parseJsonFromFile(Paths.getDirectoryManifest(Paths.getVersionsDirectory())).getAsJsonArray("versions")) {
            if(jsonElement.getAsJsonObject().get("version").getAsString().equals(pack.getGameVersion())) installMinecraft = false;
        }
        for(JsonElement jsonElement : FileUtils.parseJsonFromFile(Paths.getDirectoryManifest(Paths.getAssetsDirectory())).getAsJsonArray("assets")) {
            if(jsonElement.getAsJsonObject().get("version").getAsString().equals(versionManifest.get("assets").getAsString())) installAssets = false;
        }
        for(JsonElement jsonElement : FileUtils.parseJsonFromFile(Paths.getDirectoryManifest(Paths.getForgeDirectory())).getAsJsonArray("forgeVersions")) {
            if(jsonElement.getAsJsonObject().get("version").getAsString().equals(pack.getForgeVersion())) installForge = false;
        }
        for(JsonElement jsonElement : FileUtils.parseJsonFromFile(Paths.getDirectoryManifest(Paths.getInstancesDirectory())).getAsJsonArray("packs")) {
            if(jsonElement.getAsJsonObject().get("name").getAsString().equals(pack.getName()) && jsonElement.getAsJsonObject().get("version").getAsString().equals(pack.getVersion())) installPack = false;
        }

        int totalSteps = optionalMods.size();
        int completedSteps = 0;
        if(installMinecraft) totalSteps += 2;
        if(installAssets) totalSteps++;
        if(installForge) totalSteps += 2;
        if(installPack) totalSteps += 3;
        setTotalProgressPercent(completedSteps, totalSteps);

        if(installMinecraft) {
            setProgressPercent(0, 1);
            installMinecraft(versionManifest, completedSteps, totalSteps);
            completedSteps += 2;
            setTotalProgressPercent(completedSteps, totalSteps);
        }
        if(installAssets) {
            setProgressPercent(0, 1);
            installAssets(versionManifest, completedSteps, totalSteps);
            completedSteps++;
            setTotalProgressPercent(completedSteps, totalSteps);
        }
        if(installForge) {
            setProgressText("TEST COMPLETE");
        }
    }

    public static void setProgressText(String text) {
        Platform.runLater(() -> ((Text) Install.getInstance().getNotificationText().getChildren().get(0)).setText(text));
    }

    public static void setProgressPercent(int completed, int total) {
        Platform.runLater(() -> Install.getInstance().getLoadingBar().setProgress(((double)completed) / total));
    }

    public static void setTotalProgressPercent(int completed, int total) {
        Platform.runLater(() -> Install.getInstance().getBottomBar().setProgress(((double)completed) / total));
    }

    public static void installMinecraft(JsonObject versionManifest, int completedSteps, int totalSteps) throws IOException {
        setProgressText("Installing Minecraft " + versionManifest.get("id").getAsString());
        File versionFolder = new File(Paths.getVersionsDirectory() + File.separator + versionManifest.get("id").getAsString());
        FileUtils.deleteDirectory(versionFolder);
        versionFolder.mkdirs();

        // Write version JSON manifest
        FileUtils.writeJsonToFile(new File(versionFolder.getPath() + File.separator + versionManifest.get("id").getAsString() + ".json"), versionManifest);
        setProgressPercent(1, 2);

        // Download jar
        FileUtils.copyURLToFile(versionManifest.getAsJsonObject("downloads").getAsJsonObject("client").get("url").getAsString(), new File(versionFolder.getPath() + File.separator + versionManifest.get("id").getAsString() + ".jar"));
        setTotalProgressPercent(completedSteps + 1, totalSteps);

        // Download Libraries
        setProgressText("Downloading Libraries");
        int completedLibraries = 0;
        int totalLibraries = versionManifest.getAsJsonArray("libraries").size();
        setProgressPercent(completedLibraries, totalLibraries);
        File librariesFolder = new File(versionFolder.getPath() + File.separator + "libraries");
        librariesFolder.mkdirs();
        File nativesFolder = new File(versionFolder.getPath() + File.separator + "natives");
        nativesFolder.mkdirs();
        String librariesLaunchCode = "";

        libraryLoop:
        for(JsonElement libraryElement : versionManifest.getAsJsonArray("libraries")) {
            JsonObject library = libraryElement.getAsJsonObject();
            // Check if the library has conditions
            if(library.has("rules")) {
                for(JsonElement rule : library.getAsJsonArray("rules")) {
                    switch(rule.getAsJsonObject().get("action").getAsString()) {
                        case "allow":
                            if(!rule.getAsJsonObject().has("os")) break;
                            switch(rule.getAsJsonObject().getAsJsonObject("os").get("name").getAsString()) {
                                case "windows":
                                    if(!SystemUtils.IS_OS_WINDOWS) {
                                        completedLibraries++;
                                        continue libraryLoop;
                                    }
                                case "osx":
                                    if(!SystemUtils.IS_OS_MAC_OSX) {
                                        completedLibraries++;
                                        continue libraryLoop;
                                    }
                                case "linux":
                                    if(!SystemUtils.IS_OS_LINUX) {
                                        completedLibraries++;
                                        continue libraryLoop;
                                    }
                            }
                            break;
                        case "disallow":
                            if(!rule.getAsJsonObject().has("os")) break;
                            switch(rule.getAsJsonObject().getAsJsonObject("os").get("name").getAsString()) {
                                case "windows":
                                    if(SystemUtils.IS_OS_WINDOWS) {
                                        completedLibraries++;
                                        continue libraryLoop;
                                    }
                                case "osx":
                                    if(SystemUtils.IS_OS_MAC_OSX) {
                                        completedLibraries++;
                                        continue libraryLoop;
                                    }
                                case "linux":
                                    if(SystemUtils.IS_OS_LINUX) {
                                        completedLibraries++;
                                        continue libraryLoop;
                                    }
                            }
                    }
                }
            }
            // The library is not conditional. Continue with the download.
            JsonObject libraryDownloads = library.getAsJsonObject("downloads");
            // Download any standard libraries
            if(libraryDownloads.has("artifact")) {
                new File(librariesFolder + File.separator + StringUtils.substringBeforeLast(libraryDownloads.getAsJsonObject("artifact").get("path").getAsString(), "/").replace("/", File.separator)).mkdirs();
                String filePath = librariesFolder.getPath() + File.separator + libraryDownloads.getAsJsonObject("artifact").get("path").getAsString().replace("/", File.separator);
                FileUtils.copyURLToFile(libraryDownloads.getAsJsonObject("artifact").get("url").getAsString(), new File(filePath));
                librariesLaunchCode += filePath;
                librariesLaunchCode += ";";
            }
            // Download any natives
            if(libraryDownloads.has("classifiers")) {
                String nativesType = "";
                if(SystemUtils.IS_OS_WINDOWS) nativesType = "natives-windows";
                if(SystemUtils.IS_OS_MAC_OSX) nativesType = "natives-osx";
                if(SystemUtils.IS_OS_LINUX) nativesType = "natives-linux";
                if(libraryDownloads.getAsJsonObject("classifiers").has(nativesType)) {
                    JsonObject nativeJson = libraryDownloads.getAsJsonObject("classifiers").getAsJsonObject(nativesType);
                    File outputFile = new File(nativesFolder + File.separator + nativeJson.get("sha1").getAsString());
                    FileUtils.copyURLToFile(nativeJson.get("url").getAsString(), outputFile);
                    FileUtils.extractJar(outputFile.getPath(), nativesFolder.getPath());
                    outputFile.delete();
                }
            }
            completedLibraries++;
            setProgressPercent(completedLibraries, totalLibraries);
        }

        // Populate Versions Manifest
        JsonObject versionJsonObject = new JsonObject();
        versionJsonObject.addProperty("version", versionManifest.get("id").getAsString());
        versionJsonObject.addProperty("classpathLibraries", StringUtils.substringBeforeLast(librariesLaunchCode, ";"));
        JsonObject versionsManifest = FileUtils.parseJsonFromFile(Paths.getDirectoryManifest(Paths.getVersionsDirectory()));
        versionsManifest.getAsJsonArray("versions").add(versionJsonObject);
        FileUtils.writeJsonToFile(new File(Paths.getDirectoryManifest(Paths.getVersionsDirectory()).getPath()), versionsManifest);
    }

    public static void installAssets(JsonObject versionManifest, int completedSteps, int totalSteps) throws IOException {
        setProgressText("Downloading Assets");
        File assetsFolder = new File(Paths.getAssetsDirectory() + File.separator + versionManifest.get("assets").getAsString());
        FileUtils.deleteDirectory(assetsFolder);
        assetsFolder.mkdirs();

        // Write assets JSON manifest
        JsonObject assetsManifest = JsonFetcher.getJsonFromUrl(versionManifest.getAsJsonObject("assetIndex").get("url").getAsString());
        new File(assetsFolder.getPath() + File.separator + "indexes").mkdirs();
        FileUtils.writeJsonToFile(new File(assetsFolder.getPath() + File.separator + "indexes" + File.separator + versionManifest.get("assets").getAsString() + ".json"), assetsManifest);

        // Download assets
        int completedAssets = 0;
        int totalAssets = assetsManifest.getAsJsonObject("objects").keySet().size();
        setProgressPercent(completedAssets, totalAssets);
        for(String assetKey : assetsManifest.getAsJsonObject("objects").keySet()) {
            String hash = assetsManifest.getAsJsonObject("objects").getAsJsonObject(assetKey).get("hash").getAsString();
            File specificAssetFolder = new File(assetsFolder.getPath() + File.separator + "objects" + File.separator + hash.substring(0, 2));
            specificAssetFolder.mkdirs();
            FileUtils.copyURLToFile("http://resources.download.minecraft.net/" + hash.substring(0, 2) + "/" + hash, new File(specificAssetFolder.getPath() + File.separator + hash));
            completedAssets++;
            setProgressPercent(completedAssets, totalAssets);
        }

        // Populate Assets Manifest
        JsonObject assetsVersionJsonObject = new JsonObject();
        assetsVersionJsonObject.addProperty("version", versionManifest.get("assets").getAsString());
        JsonObject assetsFolderManifest = FileUtils.parseJsonFromFile(Paths.getDirectoryManifest(Paths.getAssetsDirectory()));
        assetsFolderManifest.getAsJsonArray("assets").add(assetsVersionJsonObject);
        FileUtils.writeJsonToFile(new File(Paths.getDirectoryManifest(Paths.getAssetsDirectory()).getPath()), assetsFolderManifest);
    }

}
