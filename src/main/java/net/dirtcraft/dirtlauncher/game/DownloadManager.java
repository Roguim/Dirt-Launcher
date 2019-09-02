package net.dirtcraft.dirtlauncher.game;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.therandomlabs.utils.io.NetUtils;
import javafx.application.Platform;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import net.dirtcraft.dirtlauncher.Data.Config;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.game.objects.OptionalMod;
import net.dirtcraft.dirtlauncher.gui.home.login.ActionButton;
import net.dirtcraft.dirtlauncher.gui.home.sidebar.Pack;
import net.dirtcraft.dirtlauncher.gui.wizards.Install;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.dirtcraft.dirtlauncher.utils.WebUtils;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class DownloadManager {

    public static void completePackSetup(Pack pack, List<OptionalMod> optionalMods, boolean isUpdate) throws IOException  {
        JsonObject versionManifest = WebUtils.getVersionManifestJson(pack.getGameVersion());

        boolean installMinecraft = true;
        boolean installAssets = true;
        boolean installForge = true;
        boolean installPack = true;
        boolean updatePack = isUpdate;
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
        if(isUpdate) {
            installPack = false;
        } else {
            for(JsonElement jsonElement : FileUtils.readJsonFromFile(settings.getDirectoryManifest(settings.getInstancesDirectory())).getAsJsonArray("packs")) {
                if(jsonElement.getAsJsonObject().get("name").getAsString().equals(pack.getName()) && jsonElement.getAsJsonObject().get("version").getAsString().equals(pack.getVersion())) installPack = false;
            }
        }

        int packStageSteps = 0;
        if(isUpdate) {
            switch(pack.getPackType()) {
                case CURSE:
                    packStageSteps = 4;
                    break;
                case CUSTOM:
                    packStageSteps = 1;
                    break;
            }
        } else {
            switch(pack.getPackType()) {
                case CURSE:
                    packStageSteps = 2;
                    break;
                case CUSTOM:
                    packStageSteps = 1;
                    break;
            }
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
            setProgressPercent(0, 1);
            installForge(pack, completedSteps, totalSteps);
            completedSteps += 3;
            setTotalProgressPercent(completedSteps, totalSteps);
        }
        if(installPack) {
            setProgressPercent(0, 1);
            installPack(pack, completedSteps, totalSteps);
            completedSteps += packStageSteps;
            setTotalProgressPercent(completedSteps, totalSteps);
        }
        if(updatePack) {
            setProgressPercent(0, 1);
            updatePack(pack, completedSteps, totalSteps);
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

    public static void installMinecraft(JsonObject versionManifest, int completedSteps, int totalSteps) throws IOException {
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
        int completedLibraries = 0;
        int totalLibraries = versionManifest.getAsJsonArray("libraries").size();
        setProgressPercent(completedLibraries, totalLibraries);
        File librariesFolder = new File(versionFolder.getPath(), "libraries");
        librariesFolder.mkdirs();
        File nativesFolder = new File(versionFolder.getPath(), "natives");
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
                                    break;
                                case "osx":
                                    if(!SystemUtils.IS_OS_MAC) {
                                        completedLibraries++;
                                        continue libraryLoop;
                                    }
                                    break;
                                case "linux":
                                    if(!SystemUtils.IS_OS_LINUX) {
                                        completedLibraries++;
                                        continue libraryLoop;
                                    }
                                    break;
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
                                    break;
                                case "osx":
                                    if(SystemUtils.IS_OS_MAC) {
                                        completedLibraries++;
                                        continue libraryLoop;
                                    }
                                    break;
                                case "linux":
                                    if(SystemUtils.IS_OS_LINUX) {
                                        completedLibraries++;
                                        continue libraryLoop;
                                    }
                                    break;
                            }
                            break;
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
                if(SystemUtils.IS_OS_MAC) nativesType = "natives-osx";
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
        JsonObject versionsManifest = FileUtils.readJsonFromFile(settings.getDirectoryManifest(settings.getVersionsDirectory()));
        versionsManifest.getAsJsonArray("versions").add(versionJsonObject);
        FileUtils.writeJsonToFile(new File(settings.getDirectoryManifest(settings.getVersionsDirectory()).getPath()), versionsManifest);
    }

    public static void installAssets(JsonObject versionManifest, int completedSteps, int totalSteps) throws IOException {
        final Config settings = Main.getConfig();
        setProgressText("Downloading Assets");
        File assetsFolder = settings.getAssetsDirectory();
        assetsFolder.mkdirs();

        // Write assets JSON manifest
        JsonObject assetsManifest = WebUtils.getJsonFromUrl(versionManifest.getAsJsonObject("assetIndex").get("url").getAsString());
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
        JsonObject assetsFolderManifest = FileUtils.readJsonFromFile(settings.getDirectoryManifest(settings.getAssetsDirectory()));
        assetsFolderManifest.getAsJsonArray("assets").add(assetsVersionJsonObject);
        FileUtils.writeJsonToFile(new File(settings.getDirectoryManifest(settings.getAssetsDirectory()).getPath()), assetsFolderManifest);
    }

    public static void installForge(Pack pack, int completedSteps, int totalSteps) throws IOException {
        final Config settings = Main.getConfig();
        setProgressText("Downloading Forge Installer");
        File forgeFolder = new File(settings.getForgeDirectory() + File.separator + pack.getForgeVersion());
        FileUtils.deleteDirectory(forgeFolder);
        forgeFolder.mkdirs();

        // Download Forge Installer
        File forgeInstaller = new File(forgeFolder.getPath() + File.separator + "installer.jar");
        // 1.7 did some strange stuff with forge file names
        if(pack.getGameVersion().equals("1.7.10")) FileUtils.copyURLToFile("https://files.minecraftforge.net/maven/net/minecraftforge/forge/" + pack.getGameVersion() + "-" + pack.getForgeVersion() + "-" + pack.getGameVersion() + "/forge-" + pack.getGameVersion() + "-" + pack.getForgeVersion() + "-" + pack.getGameVersion() + "-installer.jar", forgeInstaller);
        else FileUtils.copyURLToFile("https://files.minecraftforge.net/maven/net/minecraftforge/forge/" + pack.getGameVersion() + "-" + pack.getForgeVersion() + "/forge-" + pack.getGameVersion() + "-" + pack.getForgeVersion() + "-installer.jar", forgeInstaller);

        // Extract Forge Installer & Write forge JSON manifest
        setProgressText("Extracting Forge Installer");
        setTotalProgressPercent(completedSteps + 1, totalSteps);
        JsonObject forgeVersionManifest = FileUtils.extractForgeJar(forgeInstaller, forgeFolder.getPath());
        forgeInstaller.delete();
        setProgressPercent(1, 2);
        FileUtils.writeJsonToFile(new File(forgeFolder.getPath() + File.separator + pack.getForgeVersion() + ".json"), forgeVersionManifest);

        // Download forge libraries
        setProgressText("Downloading Forge Libraries");
        setTotalProgressPercent(completedSteps + 2, totalSteps);
        setProgressPercent(0, 1);
        int completedLibraries = 0;
        int totalLibraries = forgeVersionManifest.getAsJsonObject("versionInfo").getAsJsonArray("libraries").size() - 1;
        String librariesLaunchCode = "";

        for (JsonElement libraryElement : forgeVersionManifest.getAsJsonObject("versionInfo").getAsJsonArray("libraries")) {
            JsonObject library = libraryElement.getAsJsonObject();
            String[] libraryMaven = library.get("name").getAsString().split(":");
            // We already got forge
            if (libraryMaven[1].equals("forge")) {
                completedLibraries++;
                setProgressPercent(completedLibraries, totalLibraries);
                continue;
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
            librariesLaunchCode += StringUtils.substringBeforeLast(libraryFile.getPath(), ".pack.xz") + ";";
            completedLibraries++;
            setProgressPercent(completedLibraries, totalLibraries);
        }

        JsonObject forgeManifest = FileUtils.readJsonFromFile(settings.getDirectoryManifest(settings.getForgeDirectory()));
        JsonObject versionJsonObject = new JsonObject();
        versionJsonObject.addProperty("version", pack.getForgeVersion());
        versionJsonObject.addProperty("classpathLibraries", StringUtils.substringBeforeLast(forgeFolder + File.separator + "forge-" + pack.getGameVersion() + "-" + pack.getForgeVersion() + "-universal.jar;" + librariesLaunchCode, ";"));
        forgeManifest.getAsJsonArray("forgeVersions").add(versionJsonObject);
        FileUtils.writeJsonToFile(new File(settings.getDirectoryManifest(settings.getForgeDirectory()).getPath()), forgeManifest);
    }

    public static void installPack(Pack pack, int completedSteps, int totalSteps) throws IOException {
        final Config settings = Main.getConfig();
        setProgressText("Downloading ModPack Manifest");

        // These values will never change
        final File modpackFolder = new File(settings.getInstancesDirectory() + File.separator + pack.getName().replaceAll("\\s", "-"));
        final File modpackZip = new File(modpackFolder.getPath() + File.separator + "modpack.zip");
        final File tempDir = new File(modpackFolder.getPath() + File.separator + "temp");

        // Delete directory if exists and make new ones
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
                FileUtils.copyDirectory(new File(tempDir.getPath() + File.separator + "overrides"), modpackFolder);
                JsonObject modpackManifest = FileUtils.readJsonFromFile(new File(tempDir.getPath() + File.separator + "manifest.json"));
                FileUtils.writeJsonToFile(new File(modpackFolder.getPath() + File.separator + "manifest.json"), modpackManifest);
                FileUtils.deleteDirectory(tempDir);
                setProgressPercent(0, 0);
                setTotalProgressPercent(completedSteps + 1, totalSteps);

                // Download Mods
                setProgressText("Downloading Mods");
                int completedMods = 0;
                int totalMods = modpackManifest.getAsJsonArray("files").size();
                File modsFolder = new File(modpackFolder.getPath() + File.separator + "mods");

                for (JsonElement modElement : modpackManifest.getAsJsonArray("files")) {

                    JsonObject mod = modElement.getAsJsonObject();
                    JsonObject apiResponse = WebUtils.getJsonFromUrl("https://addons-ecs.forgesvc.net/api/v2/addon/" + mod.get("projectID").getAsString() + "/file/" + mod.get("fileID").getAsString());
                    FileUtils.copyURLToFile(apiResponse.get("downloadUrl").getAsString().replaceAll("\\s", "%20"), new File(modsFolder.getPath() + File.separator + apiResponse.get("fileName").getAsString()));
                    completedMods++;
                    setProgressPercent(completedMods, totalMods);
                }

                break;
        }

        JsonObject instanceManifest = FileUtils.readJsonFromFile(settings.getDirectoryManifest(settings.getInstancesDirectory()));
        JsonObject packJson = new JsonObject();
        packJson.addProperty("name", pack.getName());
        packJson.addProperty("version", pack.getVersion());
        packJson.addProperty("gameVersion", pack.getGameVersion());
        packJson.addProperty("forgeVersion", pack.getForgeVersion());
        instanceManifest.getAsJsonArray("packs").add(packJson);
        FileUtils.writeJsonToFile(new File(settings.getDirectoryManifest(settings.getInstancesDirectory()).getPath()), instanceManifest);
    }

    public static void updatePack(Pack pack, int completedSteps, int totalSteps) throws IOException {
        final Config settings = Main.getConfig();
        setProgressText("Downloading ModPack Manifest");

        // Get the modpack directory
        final File modpackFolder = new File(settings.getInstancesDirectory() + File.separator + pack.getName().replace(" ", "-"));
        final File modpackZip = new File(modpackFolder.getPath() + File.separator + "modpack.zip");
        final File tempDir = new File(modpackFolder.getPath() + File.separator + "temp");

        switch(pack.getPackType()) {
            default:
                //TODO ERR
                System.out.println("Could not identify pack type. Please report IMMEDIATELY!");
                return;
            case CUSTOM:

                //Delete mods and configs directories if exists and make a new directory for the modpack if it does not exist
                if (!modpackFolder.exists()) modpackFolder.mkdirs();
                else {
                    FileUtils.deleteDirectory(new File(modpackFolder.getPath() + "mods"));
                    FileUtils.deleteDirectory(new File(modpackFolder.getPath() + "config"));
                }

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
                FileUtils.copyDirectory(new File(tempDir.getPath() + File.separator + "overrides"), modpackFolder);
                File oldManifestFile = new File(modpackFolder.getPath() + File.separator + "manifest.json");
                JsonObject oldManifest = FileUtils.readJsonFromFile(oldManifestFile);
                JsonObject newManifest = FileUtils.readJsonFromFile(new File(tempDir.getPath() + File.separator + "manifest.json"));
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
                    modsToDelete.add(new ImmutablePair<>(modElement.getAsJsonObject().get("projectID").getAsInt(), modElement.getAsJsonObject().get("fileID").getAsInt()));
                }
                for(JsonElement modElement : newManifest.getAsJsonArray("files")) {
                    modsToAdd.add(new ImmutablePair<>(modElement.getAsJsonObject().get("projectID").getAsInt(), modElement.getAsJsonObject().get("fileID").getAsInt()));
                }

                // If any mods are the same in both lists, remove them. No need to repeat work
                ListIterator<Pair<Integer, Integer>> iterator = modsToDelete.listIterator();
                while(iterator.hasNext()) {
                    if(modsToAdd.contains(iterator.next())) {
                        modsToAdd.remove(iterator.next());
                        iterator.remove();
                    }
                }

                setTotalProgressPercent(completedSteps + 3, totalSteps);
                setProgressPercent(0, 0);
                setProgressText("Updating Mods");
                int completedMods = 0;
                int totalMods = modsToDelete.size() + modsToAdd.size();

                // Delete old mods
                for(Pair<Integer, Integer> oldMod : modsToDelete) {
                    JsonObject apiResponse = WebUtils.getJsonFromUrl("https://addons-ecs.forgesvc.net/api/v2/addon/" + oldMod.getKey() + "/file/" + oldMod.getValue());
                    new File(modpackFolder.getPath() + File.separator + "mods" + File.separator + apiResponse.get("fileName").getAsString()).delete();
                    completedMods++;
                    setProgressPercent(completedMods, totalMods);
                }

                // Download new mods
                for(Pair<Integer, Integer> newMod : modsToDelete) {
                    JsonObject apiResponse = WebUtils.getJsonFromUrl("https://addons-ecs.forgesvc.net/api/v2/addon/" + newMod.getKey() + "/file/" + newMod.getValue());
                    FileUtils.copyURLToFile(apiResponse.get("downloadUrl").getAsString().replaceAll("\\s", "%20"), new File(modpackFolder.getPath() + File.separator + "mods" + File.separator + apiResponse.get("fileName").getAsString()));
                    completedMods++;
                    setProgressPercent(completedMods, totalMods);
                }
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
}
