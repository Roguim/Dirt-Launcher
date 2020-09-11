package net.dirtcraft.dirtlauncher.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.therandomlabs.utils.io.NetUtils;
import javafx.application.Platform;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import net.dirtcraft.dirtlauncher.Data.Config;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.game.installation.Pair;
import net.dirtcraft.dirtlauncher.game.objects.OptionalMod;
import net.dirtcraft.dirtlauncher.gui.home.login.ActionButton;
import net.dirtcraft.dirtlauncher.gui.home.sidebar.Pack;
import net.dirtcraft.dirtlauncher.gui.wizards.Install;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.dirtcraft.dirtlauncher.utils.WebUtils;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static net.dirtcraft.dirtlauncher.utils.Constants.*;

public class DownloadManager {

    public static void completePackSetup(Pack pack, List<OptionalMod> optionalMods, boolean updatePack) throws IOException  {
        final ExecutorService downloadManager = Executors.newFixedThreadPool(MAX_DOWNLOAD_THREADS);
        final JsonObject versionManifest = WebUtils.getVersionManifestJson(pack.getGameVersion());

        boolean installMinecraft = true;
        boolean installAssets = true;
        boolean installForge = true;
        boolean installPack = true;
        final Config settings = Main.getConfig();

        for(JsonElement jsonElement : FileUtils.readJsonFromFile(settings.getDirectoryManifest(settings.getVersionsDirectory())).getAsJsonArray("versions")) {
            if(jsonElement.getAsJsonObject().get("version").getAsString().equals(pack.getGameVersion())) installMinecraft = false;
        }
        for(JsonElement jsonElement : FileUtils.readJsonFromFile(settings.getDirectoryManifest(settings.getAssetsDirectory())).getAsJsonArray("assets")) {
            if(jsonElement.getAsJsonObject().get("version").getAsString().equals(versionManifest.get("assets").getAsString())) installAssets = false;
        }
        for(JsonElement jsonElement : FileUtils.readJsonFromFile(settings.getDirectoryManifest(settings.getForgeDirectory())).getAsJsonArray("forgeVersions")) {
            if(jsonElement.getAsJsonObject().get("version").getAsString().equals(pack.getForgeVersion())) installForge = false;
        }
        if(updatePack) {
            installPack = false;
        } else {
            for(JsonElement jsonElement : FileUtils.readJsonFromFile(settings.getDirectoryManifest(settings.getInstancesDirectory())).getAsJsonArray("packs")) {
                if(jsonElement.getAsJsonObject().get("name").getAsString().equals(pack.getName()) && jsonElement.getAsJsonObject().get("version").getAsString().equals(pack.getVersion())) installPack = false;
            }
        }

        final int packStageSteps;
        switch (pack.getPackType()){
            case CURSE: packStageSteps = updatePack? 4 : 2; break;
            case CUSTOM:
            default: packStageSteps = 1; break;
        }

        int totalSteps = optionalMods.size();
        int completedSteps = 0;
        if(installMinecraft) totalSteps += 2;
        if(installAssets) totalSteps++;
        if(installForge) totalSteps += 3;
        if(installPack) totalSteps += packStageSteps;
        if(updatePack) totalSteps += packStageSteps;
        setTotalProgressPercent(completedSteps, totalSteps);

        if(installMinecraft) {
            setProgressPercent(0, 1);
            installMinecraft(downloadManager, versionManifest, completedSteps, totalSteps);
            completedSteps += 2;
            setTotalProgressPercent(completedSteps, totalSteps);
        }
        if(installAssets) {
            setProgressPercent(0, 1);
            installAssets(downloadManager, versionManifest, completedSteps, totalSteps);
            completedSteps++;
            setTotalProgressPercent(completedSteps, totalSteps);
        }
        if(installForge) {
            setProgressPercent(0, 1);
            installForge(downloadManager, pack, completedSteps, totalSteps);
            completedSteps += 3;
            setTotalProgressPercent(completedSteps, totalSteps);
        }
        if(installPack) {
            setProgressPercent(0, 1);
            installPack(downloadManager, pack, completedSteps, totalSteps);
            completedSteps += packStageSteps;
            setTotalProgressPercent(completedSteps, totalSteps);
        }
        if(updatePack) {
            setProgressPercent(0, 1);
            updatePack(downloadManager, pack, completedSteps, totalSteps);
            completedSteps += packStageSteps;
            setTotalProgressPercent(completedSteps, totalSteps);
        }

        /*
                --- CODE WHEN INSTALLATION IS COMPLETE ---
         */

        setTotalProgressPercent(1, 1);
        setProgressPercent(1, 1);
        Platform.runLater(() ->
            Install.getInstance().ifPresent(install -> {
                ((Text)install.getNotificationText().getChildren().get(0)).setText("Successfully Installed " + pack.getName() + "!");
                install.getButtonPane().setVisible(true);
                Stage installStage = install.getStageUnsafe();
                if (installStage != null) installStage.setOnCloseRequest(event -> {
                    if (!install.getButtonPane().isVisible()) event.consume();
                });
                Main.getHome().getLoginBar().updatePlayButton(ActionButton.Types.PLAY);
            }));
    }

    public static void setProgressText(String text) {
        Platform.runLater(() -> Install.getInstance().ifPresent(install -> ((Text)install.getNotificationText().getChildren().get(0)).setText(text + "...")));
    }

    public static void setProgressPercent(int completed, int total) {
        Platform.runLater(() -> Install.getInstance().ifPresent(install -> install.getLoadingBar().setProgress(((double)completed) / total)));
    }

    public static void setTotalProgressPercent(int completed, int total) {
        Platform.runLater(() -> Install.getInstance().ifPresent(install -> install.getBottomBar().setProgress(((double)completed) / total)));
    }

    public static void installMinecraft(ExecutorService downloadManager, JsonObject versionManifest, int completedSteps, int totalSteps) throws IOException {
        final List<Future<Optional<IOException>>> futures = new ArrayList<>();
        final Config settings = Main.getConfig();
        setProgressText("Installing Minecraft " + versionManifest.get("id").getAsString());
        File versionFolder = new File(Main.getConfig().getVersionsDirectory(), versionManifest.get("id").getAsString());
        FileUtils.deleteDirectory(versionFolder);
        versionFolder.mkdirs();

        // Write version JSON manifest
        FileUtils.writeJsonToFile(new File(versionFolder.getPath(), versionManifest.get("id").getAsString() + ".json"), versionManifest);
        setProgressPercent(1, 2);

        // Download jar
        FileUtils.copyURLToFile(versionManifest.getAsJsonObject("downloads").getAsJsonObject("client").get("url").getAsString(), new File(versionFolder.getPath(), versionManifest.get("id").getAsString() + ".jar"));
        setTotalProgressPercent(completedSteps + 1, totalSteps);

        // Download Libraries
        setProgressText("Downloading Libraries");
        AtomicInteger progress = new AtomicInteger(0);
        int totalLibs = versionManifest.getAsJsonArray("libraries").size();
        setProgressPercent(progress.get(), totalLibs);
        File libDir = new File(versionFolder.getPath(), "libraries");
        libDir.mkdirs();
        File nativeDir = new File(versionFolder.getPath(), "natives");
        nativeDir.mkdirs();
        StringBuffer launchPaths = new StringBuffer();

        for(JsonElement lib : versionManifest.getAsJsonArray("libraries")) {
            Future<Optional<IOException>> future;
            future = downloadManager.submit(()-> checkLib(lib, launchPaths, libDir, nativeDir, progress, totalLibs, 0));
            futures.add(future);
        }

        if (futures.stream().anyMatch(DownloadManager::hasFailed)) throw new IOException();

        // Populate Versions Manifest
        JsonObject versionJsonObject = new JsonObject();
        versionJsonObject.addProperty("version", versionManifest.get("id").getAsString());
        versionJsonObject.addProperty("classpathLibraries", StringUtils.substringBeforeLast(launchPaths.toString(), ";"));
        JsonObject versionsManifest = FileUtils.readJsonFromFile(settings.getDirectoryManifest(settings.getVersionsDirectory()));
        versionsManifest.getAsJsonArray("versions").add(versionJsonObject);
        FileUtils.writeJsonToFile(new File(settings.getDirectoryManifest(settings.getVersionsDirectory()).getPath()), versionsManifest);
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

    private static Optional<IOException> checkLib(JsonElement lib, StringBuffer launchPaths, File libDir, File nativeDir, AtomicInteger progress, int totalLibs, int attempts){
        JsonObject library = lib.getAsJsonObject();
        // Check if the library has conditions
        if (library.has("rules")) {
            for (JsonElement ruleElement : library.getAsJsonArray("rules")) {
                final JsonObject rule = ruleElement.getAsJsonObject();
                final String action = rule.get("action").getAsString();

                if (!(rule.has("os") && (action.equals("allow") || action.equals("disallow")))) continue;
                final String os = rule.getAsJsonObject("os").get("name").getAsString();

                if (!isUserOs(os).map(b -> b == (action.equals("allow"))).orElse(true)) continue;
                progress.incrementAndGet();
                return Optional.empty();
            }
        }
        // The library is not conditional. Continue with the download.
        JsonObject libraryDownloads = library.getAsJsonObject("downloads");
        try {
            downloadLib(libraryDownloads, launchPaths, libDir, nativeDir, progress, totalLibs);
            return Optional.empty();
        } catch (IOException e){
            return Optional.of(e);
        }
    }

    private static void downloadLib(JsonObject libraryDownloads, StringBuffer launchPaths, File libDir, File nativeDir, AtomicInteger progress, int totalLibs) throws IOException {
        // Download any standard libraries
        if (libraryDownloads.has("artifact")) {
            new File(libDir, StringUtils.substringBeforeLast(libraryDownloads.getAsJsonObject("artifact").get("path").getAsString(), "/").replace("/", File.separator)).mkdirs();
            String filePath = libDir.getPath() + File.separator + libraryDownloads.getAsJsonObject("artifact").get("path").getAsString().replace("/", File.separator);
            FileUtils.copyURLToFile(libraryDownloads.getAsJsonObject("artifact").get("url").getAsString(), new File(filePath));
            synchronized (launchPaths) {
                launchPaths.append(filePath);
                launchPaths.append(";");
            }
        }
        // Download any natives
        if (libraryDownloads.has("classifiers")) {
            String nativesType = "";
            if (SystemUtils.IS_OS_WINDOWS) nativesType = "natives-windows";
            if (SystemUtils.IS_OS_MAC) nativesType = "natives-osx";
            if (SystemUtils.IS_OS_LINUX) nativesType = "natives-linux";
            if (libraryDownloads.getAsJsonObject("classifiers").has(nativesType)) {
                JsonObject nativeJson = libraryDownloads.getAsJsonObject("classifiers").getAsJsonObject(nativesType);
                File outputFile = new File(nativeDir, nativeJson.get("sha1").getAsString());
                FileUtils.copyURLToFile(nativeJson.get("url").getAsString(), outputFile);
                FileUtils.extractJar(outputFile.getPath(), nativeDir.getPath());
                outputFile.delete();
            }
        }
        setProgressPercent(progress.incrementAndGet(), totalLibs);
        return;
    }

    public static void installAssets(ExecutorService downloadManager, JsonObject versionManifest, int completedSteps, int totalSteps) throws IOException {
        final List<Future<Optional<IOException>>> futures = new ArrayList<>();
        final Config settings = Main.getConfig();
        setProgressText("Downloading Assets");
        File assetsFolder = settings.getAssetsDirectory();
        assetsFolder.mkdirs();

        // Write assets JSON manifest
        JsonObject assetsManifest = WebUtils.getJsonFromUrl(versionManifest.getAsJsonObject("assetIndex").get("url").getAsString());
        final File indexes = new File(assetsFolder, "indexes");
        indexes.mkdirs();
        FileUtils.writeJsonToFile(new File(indexes, versionManifest.get("assets").getAsString() + ".json"), assetsManifest);

        // Download assets
        AtomicInteger completedAssets = new AtomicInteger(0);
        int totalAssets = assetsManifest.getAsJsonObject("objects").keySet().size();
        setProgressPercent(completedAssets.get(), totalAssets);
        for(String assetKey : assetsManifest.getAsJsonObject("objects").keySet()) {
            Future<Optional<IOException>> future;
            future = downloadManager.submit(()->downloadAsset(assetsManifest, assetKey, assetsFolder, completedAssets, totalAssets, 0));
            futures.add(future);
        }

        if (futures.stream().anyMatch(DownloadManager::hasFailed)) throw new IOException();

        // Populate Assets Manifest
        JsonObject assetsVersionJsonObject = new JsonObject();
        assetsVersionJsonObject.addProperty("version", versionManifest.get("assets").getAsString());
        JsonObject assetsFolderManifest = FileUtils.readJsonFromFile(settings.getDirectoryManifest(settings.getAssetsDirectory()));
        assetsFolderManifest.getAsJsonArray("assets").add(assetsVersionJsonObject);
        FileUtils.writeJsonToFile(new File(settings.getDirectoryManifest(settings.getAssetsDirectory()).getPath()), assetsFolderManifest);
    }

    public static Optional<IOException> downloadAsset(JsonObject assetsManifest, String assetKey, File assetsFolder, AtomicInteger completedAssets, int totalAssets, int attempts){
        try {
            String hash = assetsManifest.getAsJsonObject("objects").getAsJsonObject(assetKey).get("hash").getAsString();
            File specificAssetFolder = new File(new File(assetsFolder, "objects"), hash.substring(0, 2));
            specificAssetFolder.mkdirs();
            FileUtils.copyURLToFile("http://resources.download.minecraft.net/" + hash.substring(0, 2) + "/" + hash, new File(specificAssetFolder.getPath(), hash));
            setProgressPercent(completedAssets.incrementAndGet(), totalAssets);
            return Optional.empty();
        } catch (IOException e){
            return Optional.of(e);
        }
    }

    public static void installForge(ExecutorService downloadManager, Pack pack, int completedSteps, int totalSteps) throws IOException {
        final List<Future<Optional<IOException>>> futures = new ArrayList<>();
        final Config settings = Main.getConfig();
        setProgressText("Downloading Forge Installer");
        File forgeFolder = new File(settings.getForgeDirectory(), pack.getForgeVersion());
        FileUtils.deleteDirectory(forgeFolder);
        forgeFolder.mkdirs();

        // Download Forge Installer
        File forgeInstaller = new File(forgeFolder, "installer.jar");
        // 1.7 did some strange stuff with forge file names
        if(pack.getGameVersion().equals("1.7.10")) FileUtils.copyURLToFile("https://files.minecraftforge.net/maven/net/minecraftforge/forge/" + pack.getGameVersion() + "-" + pack.getForgeVersion() + "-" + pack.getGameVersion() + "/forge-" + pack.getGameVersion() + "-" + pack.getForgeVersion() + "-" + pack.getGameVersion() + "-installer.jar", forgeInstaller);
        else FileUtils.copyURLToFile("https://files.minecraftforge.net/maven/net/minecraftforge/forge/" + pack.getGameVersion() + "-" + pack.getForgeVersion() + "/forge-" + pack.getGameVersion() + "-" + pack.getForgeVersion() + "-installer.jar", forgeInstaller);

        // Extract Forge Installer & Write forge JSON manifest
        setProgressText("Extracting Forge Installer");
        setTotalProgressPercent(completedSteps + 1, totalSteps);
        JsonObject forgeVersionManifest = FileUtils.extractForgeJar(forgeInstaller, forgeFolder);
        forgeInstaller.delete();
        setProgressPercent(1, 2);
        FileUtils.writeJsonToFile(new File(forgeFolder, pack.getForgeVersion() + ".json"), forgeVersionManifest);

        // Download forge libraries
        setProgressText("Downloading Forge Libraries");
        setTotalProgressPercent(completedSteps + 2, totalSteps);
        setProgressPercent(0, 1);
        AtomicInteger completedLibraries = new AtomicInteger(0);
        int totalLibraries = forgeVersionManifest.getAsJsonObject("versionInfo").getAsJsonArray("libraries").size() - 1;
        StringBuffer librariesLaunchCode = new StringBuffer();

        for (JsonElement libraryElement : forgeVersionManifest.getAsJsonObject("versionInfo").getAsJsonArray("libraries")) {
            Future<Optional<IOException>> future;
            future = downloadManager.submit(()->getForgeLibrary(libraryElement, librariesLaunchCode, forgeFolder, completedLibraries, totalLibraries, 0));
            futures.add(future);
        }

        if (futures.stream().anyMatch(DownloadManager::hasFailed)) throw new IOException();

        JsonObject forgeManifest = FileUtils.readJsonFromFile(settings.getDirectoryManifest(settings.getForgeDirectory()));
        JsonObject versionJsonObject = new JsonObject();
        versionJsonObject.addProperty("version", pack.getForgeVersion());
        versionJsonObject.addProperty("classpathLibraries", StringUtils.substringBeforeLast(forgeFolder + File.separator + "forge-" + pack.getGameVersion() + "-" + pack.getForgeVersion() + "-universal.jar;" + librariesLaunchCode.toString(), ";"));
        forgeManifest.getAsJsonArray("forgeVersions").add(versionJsonObject);
        FileUtils.writeJsonToFile(new File(settings.getDirectoryManifest(settings.getForgeDirectory()).getPath()), forgeManifest);
    }

    public static Optional<IOException> getForgeLibrary(JsonElement libraryElement, StringBuffer librariesLaunchCode, File forgeFolder, AtomicInteger completedLibraries, int totalLibraries, int attempts){
        try {
            JsonObject library = libraryElement.getAsJsonObject();
            String[] libraryMaven = library.get("name").getAsString().split(":");
            // We already got forge
            if (libraryMaven[1].equals("forge")) {
                setProgressPercent(completedLibraries.incrementAndGet(), totalLibraries);
                return Optional.empty();
            }
            File libraryPath = new File(forgeFolder + File.separator + "libraries" + File.separator + libraryMaven[0].replace(".", File.separator) + File.separator + libraryMaven[1] + File.separator + libraryMaven[2]);
            libraryPath.mkdirs();
            String url = "https://libraries.minecraft.net/";
            if (library.has("url")) {
                url = library.get("url").getAsString();
            }
            url += libraryMaven[0].replace(".", "/") + "/" + libraryMaven[1] + "/" + libraryMaven[2] + "/" + libraryMaven[1] + "-" + libraryMaven[2] + ".jar";

            String fileName = libraryPath + File.separator + libraryMaven[1] + "-" + libraryMaven[2] + ".jar";
            // Typesafe does some weird crap
            if (libraryMaven[0].contains("typesafe")) {
                url += ".pack.xz";
                fileName += ".pack.xz";
            }

            File libraryFile = new File(fileName);
            FileUtils.copyURLToFile(url, libraryFile);
            if (libraryFile.getName().contains(".pack.xz")) {
                FileUtils.unpackPackXZ(libraryFile);
            }
            synchronized (librariesLaunchCode){
                librariesLaunchCode.append(StringUtils.substringBeforeLast(libraryFile.getPath(), ".pack.xz"));
                librariesLaunchCode.append(";");
            }
            setProgressPercent(completedLibraries.incrementAndGet(), totalLibraries);
            return Optional.empty();
        } catch (IOException e){
            return Optional.of(e);
        }
    }

    public static void installPack(ExecutorService downloadManager, Pack pack, int completedSteps, int totalSteps) throws IOException {
        final List<Future<Optional<IOException>>> futures = new ArrayList<>();
        final Config settings = Main.getConfig();
        setProgressText("Downloading ModPack Manifest");

        // These values will never change
        final File modpackFolder = pack.getInstanceDirectory();
        final File modpackZip = new File(modpackFolder.getPath(), "modpack.zip");
        final File tempDir = new File(modpackFolder.getPath(), "temp");

        // Delete directory if exists and make new ones`
        FileUtils.deleteDirectory(modpackFolder);
        modpackFolder.mkdirs();

        switch (pack.getPackType()) {
            default:
                //TODO ERR
                System.out.println("Could not identify pack type. Please report IMMEDIATELY!");
                return;
            case CUSTOM:
                setProgressText("Downloading " + pack.getName() + " Files");

                Timer timer = new Timer();
                pack.getFileSize().ifPresent(fileSize ->
                        timer.scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                setProgressPercent((int) (modpackZip.length() / 1024 / 1024), fileSize);
                            }
                        }, 0, 1000));
                FileUtils.copyURLToFile(pack.getLink(), modpackZip);
                timer.cancel();
                setProgressText("Extracting " + pack.getName() + " Files");
                setTotalProgressPercent(completedSteps + 1, totalSteps);
                new ZipFile(modpackZip).extractAll(modpackFolder.getPath());
                modpackZip.delete();
                break;
            case CURSE:
                // Download ModPack
                FileUtils.copyURLToFile(NetUtils.getRedirectedURL(new URL(pack.getLink())).toString().replace("%2B", "+"), modpackZip);
                setProgressPercent(1, 2);
                tempDir.mkdirs();
                new ZipFile(modpackZip).extractAll(tempDir.getPath());
                modpackZip.delete();
                FileUtils.copyDirectory(new File(tempDir.getPath(), "overrides"), modpackFolder);
                JsonObject modpackManifest = FileUtils.readJsonFromFile(new File(tempDir, "manifest.json"));
                FileUtils.writeJsonToFile(new File(modpackFolder, "manifest.json"), modpackManifest);
                FileUtils.deleteDirectory(tempDir);
                setProgressPercent(0, 0);
                setTotalProgressPercent(completedSteps + 1, totalSteps);

                // Download Mods
                setProgressText("Downloading Mods");
                File modsFolder = new File(modpackFolder.getPath(), "mods");


                final AtomicInteger completedMods = new AtomicInteger(0);
                final JsonArray mods = modpackManifest.getAsJsonArray("files");

                for (JsonElement modElement : modpackManifest.getAsJsonArray("files")) {
                    Future<Optional<IOException>> future = downloadManager.submit(()->downloadMod(modElement, modsFolder, completedMods, mods.size(), 0));
                    futures.add(future);
                }
                break;
        }

        if (futures.stream().anyMatch(DownloadManager::hasFailed)) throw new IOException();

        JsonObject instanceManifest = FileUtils.readJsonFromFile(settings.getDirectoryManifest(settings.getInstancesDirectory()));
        JsonObject packJson = new JsonObject();
        packJson.addProperty("name", pack.getName());
        packJson.addProperty("version", pack.getVersion());
        packJson.addProperty("gameVersion", pack.getGameVersion());
        packJson.addProperty("forgeVersion", pack.getForgeVersion());
        instanceManifest.getAsJsonArray("packs").add(packJson);
        FileUtils.writeJsonToFile(new File(settings.getDirectoryManifest(settings.getInstancesDirectory()).getPath()), instanceManifest);
    }

    private static Optional<IOException> downloadMod(JsonElement modElement, File modsFolder, AtomicInteger completedMods, int maxSz, int attempts){
        try {
            JsonObject mod = modElement.getAsJsonObject();
            if (mod.has("required") && !mod.get("required").getAsBoolean()) return Optional.empty();
            JsonObject apiResponse = WebUtils.getJsonFromUrl("https://addons-ecs.forgesvc.net/api/v2/addon/" + mod.get("projectID").getAsString() + "/file/" + mod.get("fileID").getAsString());
            FileUtils.copyURLToFile(apiResponse.get("downloadUrl").getAsString().replaceAll("\\s", "%20"), new File(modsFolder, apiResponse.get("fileName").getAsString()));
            setProgressPercent(completedMods.incrementAndGet(), maxSz);
            return Optional.empty();
        } catch (IOException e){
            return Optional.of(e);
        }

    }

    private static void updatePack(ExecutorService downloadManager, Pack pack, int completedSteps, int totalSteps) throws IOException {
        final List<Future<Optional<IOException>>> futures = new ArrayList<>();
        final Config settings = Main.getConfig();
        setProgressText("Downloading ModPack Manifest");

        // Get the modpack directory
        final File modpackFolder = pack.getInstanceDirectory();
        final File modpackZip = new File(modpackFolder, "modpack.zip");
        final File tempDir = new File(modpackFolder, "temp");
        final File modsDir = new File(modpackFolder, "mods");

        switch(pack.getPackType()) {
            default:
                //TODO ERR
                System.out.println("Could not identify pack type. Please report IMMEDIATELY!");
                return;
            case CUSTOM:

                FileUtils.deleteDirectory(modsDir);
                FileUtils.deleteDirectory(new File(modpackFolder.getPath(), "config"));


                setProgressText("Downloading " + pack.getName() + " Files");

                Timer timer = new Timer();
                pack.getFileSize().ifPresent(fileSize ->
                        timer.scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                setProgressPercent((int) (modpackZip.length() / 1024 / 1024), fileSize);
                            }
                        }, 0, 1000));
                FileUtils.copyURLToFile(pack.getLink(), modpackZip);
                timer.cancel();
                setProgressText("Extracting " + pack.getName() + " Files");
                setTotalProgressPercent(completedSteps + 1, totalSteps);
                new ZipFile(modpackZip).extractAll(modpackFolder.getPath());
                modpackZip.delete();
                break;

            case CURSE:
                // Download modpack
                FileUtils.copyURLToFile(NetUtils.getRedirectedURL(new URL(pack.getLink())).toString().replace("%2B", "+"), modpackZip);
                setProgressText("Extracting " + pack.getName() + " Files");
                setTotalProgressPercent(completedSteps + 1, totalSteps);
                tempDir.mkdirs();
                new ZipFile(modpackZip).extractAll(tempDir.getPath());
                modpackZip.delete();
                setProgressPercent(1, 2);
                FileUtils.copyDirectory(new File(tempDir, "overrides"), modpackFolder);
                File oldManifestFile = new File(modpackFolder, "manifest.json");
                JsonObject oldManifest = FileUtils.readJsonFromFile(oldManifestFile);
                JsonObject newManifest = FileUtils.readJsonFromFile(new File(tempDir, "manifest.json"));
                oldManifestFile.delete();
                FileUtils.writeJsonToFile(oldManifestFile, newManifest);
                FileUtils.deleteDirectory(tempDir);
                setProgressPercent(0, 0);
                setTotalProgressPercent(completedSteps + 2, totalSteps);

                // Build checksum registries
                setProgressText("Comparing Mod Manifests");
                List<Pair<Integer, Integer>> modsToDelete = new ArrayList<>();
                List<Pair<Integer, Integer>> modsToAdd = new ArrayList<>();
                for(JsonElement modElement : oldManifest.getAsJsonArray("files")) {
                    if(modElement.getAsJsonObject().has("required") && modElement.getAsJsonObject().get("required").getAsBoolean() == false) continue;
                    modsToDelete.add(new Pair(modElement.getAsJsonObject().get("projectID").getAsInt(), modElement.getAsJsonObject().get("fileID").getAsInt()));
                }
                for(JsonElement modElement : newManifest.getAsJsonArray("files")) {
                    if(modElement.getAsJsonObject().has("required") && modElement.getAsJsonObject().get("required").getAsBoolean() == false) continue;
                    modsToAdd.add(new Pair(modElement.getAsJsonObject().get("projectID").getAsInt(), modElement.getAsJsonObject().get("fileID").getAsInt()));
                }

                // If any mods are the same in both lists, remove them. No need to repeat work
                ListIterator<Pair<Integer, Integer>> iterator = modsToDelete.listIterator();
                while(iterator.hasNext()) {
                    Pair<Integer, Integer> iteratorNext = iterator.next();
                    if(modsToAdd.contains(iteratorNext)) {
                        modsToAdd.remove(iteratorNext);
                        iterator.remove();
                    }
                }

                setTotalProgressPercent(completedSteps + 3, totalSteps);
                setProgressPercent(0, 0);
                setProgressText("Updating Mods");

                AtomicInteger completedMods = new AtomicInteger(0);
                int totalMods = modsToDelete.size() + modsToAdd.size();

                // Delete old mods
                for(Pair<Integer, Integer> oldMod : modsToDelete) {
                    Future<Optional<IOException>> future;
                    future = downloadManager.submit(()->deleteMod(oldMod, modsDir, completedMods, totalMods, 0));
                    futures.add(future);
                }

                if (futures.stream().anyMatch(DownloadManager::hasFailed)) throw new IOException();
                else futures.clear();

                // Download new mods
                for(Pair<Integer, Integer> newMod : modsToAdd) {
                    Future<Optional<IOException>> future;
                    future = downloadManager.submit(()->updateMod(newMod, modpackFolder, completedMods, totalMods, 0));
                    futures.add(future);
                }
                if (futures.stream().anyMatch(DownloadManager::hasFailed)) throw new IOException();
                break;
        }
        // Update instance manifest
        JsonObject instanceManifest = FileUtils.readJsonFromFile(settings.getDirectoryManifest(settings.getInstancesDirectory()));
        for(JsonElement jsonElement : instanceManifest.getAsJsonArray("packs")) {
            if(jsonElement.getAsJsonObject().get("name").getAsString().equals(pack.getName())) {
                jsonElement.getAsJsonObject().addProperty("version", pack.getVersion());
                jsonElement.getAsJsonObject().addProperty("gameVersion", pack.getGameVersion());
                jsonElement.getAsJsonObject().addProperty("forgeVersion", pack.getForgeVersion());
            }
        }

        JsonObject newPackObject = new JsonObject();
        Iterator<JsonElement> jsonIterator = instanceManifest.getAsJsonArray("packs").iterator();
        while(jsonIterator.hasNext()) {
            JsonObject nextElement = jsonIterator.next().getAsJsonObject();
            if (nextElement.get("name").getAsString().equals(pack.getName())) {
                newPackObject.addProperty("name", pack.getName());
                newPackObject.addProperty("version", pack.getVersion());
                newPackObject.addProperty("gameVersion", pack.getGameVersion());
                newPackObject.addProperty("forgeVersion", pack.getForgeVersion());
                jsonIterator.remove();
            }
        }
        instanceManifest.getAsJsonArray("packs").add(newPackObject);
        FileUtils.writeJsonToFile(new File(settings.getDirectoryManifest(settings.getInstancesDirectory()).getPath()), instanceManifest);
        pack.updateInstallStatus();
    }

    private static Optional<IOException> updateMod(Pair<Integer, Integer> newMod, File modsDir, AtomicInteger completedMods, int totalMods, int attempts){
        try{
            JsonObject apiResponse = WebUtils.getJsonFromUrl("https://addons-ecs.forgesvc.net/api/v2/addon/" + newMod.getFirst() + "/file/" + newMod.getSecond());
            FileUtils.copyURLToFile(apiResponse.get("downloadUrl").getAsString().replaceAll("\\s", "%20"), new File(modsDir, apiResponse.get("fileName").getAsString()));
            setProgressPercent(completedMods.incrementAndGet(), totalMods);
            return Optional.empty();
        } catch (IOException e){
            return Optional.of(e);
        }
    }

    private static Optional<IOException> deleteMod(Pair<Integer, Integer> oldMod, File modpackFolder, AtomicInteger completedMods, int totalMods, int attempts) {
        JsonObject apiResponse = WebUtils.getJsonFromUrl("https://addons-ecs.forgesvc.net/api/v2/addon/" + oldMod.getFirst() + "/file/" + oldMod.getSecond());
        new File(new File(modpackFolder.getPath(), "mods"), apiResponse.get("fileName").getAsString()).delete();
        setProgressPercent(completedMods.incrementAndGet(), totalMods);
        return Optional.empty();
    }

    private static boolean hasFailed(Future<Optional<IOException>> future){
        try {
            return future.get().isPresent();
        } catch (ExecutionException | InterruptedException e){
            e.printStackTrace();
            return false;
        }

    }
}
