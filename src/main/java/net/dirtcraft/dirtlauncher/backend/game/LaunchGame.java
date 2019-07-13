package net.dirtcraft.dirtlauncher.backend.game;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.backend.config.Paths;
import net.dirtcraft.dirtlauncher.backend.objects.Account;
import net.dirtcraft.dirtlauncher.backend.objects.Pack;
import net.dirtcraft.dirtlauncher.backend.utils.FileUtils;

import java.io.File;

public class LaunchGame {

    public static void launchPack(Pack pack, Account account) {
        JsonObject config = FileUtils.readJsonFromFile(Paths.getConfiguration());

        String launchCommand = "java ";

        // RAM
        launchCommand += "-Xms" + config.get("minimum-ram").getAsString() + " M -Xmx" + config.get("maximum-ram").getAsString() + "M ";
        // Config Java Arguments
        launchCommand += config.get("java-arguments").getAsString() + " ";
        // Mojang Tricks
        launchCommand += "-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump ";
        // Natives path
        launchCommand += "-Djava.library.path=" + Paths.getVersionsDirectory().getPath() + File.separator + pack.getGameVersion() + File.separator + "natives ";
        // Classpath
        launchCommand += "-cp ";
        for (JsonElement jsonElement : FileUtils.readJsonFromFile(Paths.getDirectoryManifest(Paths.getForgeDirectory())).getAsJsonArray("forgeVersions")) {
            if (jsonElement.getAsJsonObject().get("version").getAsString().equals(pack.getForgeVersion()))
                launchCommand += jsonElement.getAsJsonObject().get("classpathLibraries").getAsString().replace("\\\\", "\\") + ";";
        }
        for (JsonElement jsonElement : FileUtils.readJsonFromFile(Paths.getDirectoryManifest(Paths.getVersionsDirectory())).getAsJsonArray("versions")) {
            if (jsonElement.getAsJsonObject().get("version").getAsString().equals(pack.getGameVersion()))
                launchCommand += jsonElement.getAsJsonObject().get("classpathLibraries").getAsString().replace("\\\\", "\\") + ";";
        }
        launchCommand += new File(Paths.getVersionsDirectory().getPath() + File.separator + pack.getGameVersion() + File.separator + pack.getGameVersion() + ".jar").getPath() + " ";

        //Loader class
        launchCommand += "net.minecraft.launchwrapper.Launch ";
        // Dimensions
        launchCommand += "--width 960 --height 540 ";
        // Username
        launchCommand += "--username " + account.getUsername();
        // Version
        launchCommand += "--version " + pack.getForgeVersion();
        // Game Dir
        launchCommand += "--gameDir " + new File(Paths.getInstancesDirectory().getPath() + File.separator + pack.getName()).getPath() + " ";
        // Assets Dir
        String assetsVersion = FileUtils.readJsonFromFile(new File(Paths.getVersionsDirectory().getPath() + File.separator + pack.getGameVersion() + File.separator + pack.getGameVersion() + ".json")).get("assets").getAsString();
        launchCommand += "--assetsDir " + new File(Paths.getAssetsDirectory().getPath() + assetsVersion).toPath() + " ";
        // Assets Index
        launchCommand += "--assetsIndex " + assetsVersion + " ";
        // UUID
        launchCommand += "--uuid " + account.getUuid() + " ";
        // Access Token
        launchCommand += "--accessToken " + account.getSession().getAccessToken() + " ";
        // User Type
        launchCommand += "--userType mojang ";
        // Tweak Class
        launchCommand += "--tweakClass net.minecraftforge.fml.common.launcher.FMLTweaker ";
        // Version Type
        launchCommand += "--versionType Forge";

        System.out.println(launchCommand);
    }

}
