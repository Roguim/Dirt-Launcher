package net.dirtcraft.dirtlauncher.backend.game;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.backend.config.Directories;
import net.dirtcraft.dirtlauncher.backend.objects.Account;
import net.dirtcraft.dirtlauncher.backend.objects.Pack;
import net.dirtcraft.dirtlauncher.backend.utils.FileUtils;

import java.io.File;
import java.io.IOException;

public class LaunchGame {

    public static void launchPack(Pack pack, Account account) {
        JsonObject config = FileUtils.readJsonFromFile(Directories.getConfiguration());

        StringBuilder command = new StringBuilder();
        command.append("java ");

        // RAM
        command.append("-Xms" + config.get("minimum-ram").getAsString() + "M -Xmx" + config.get("maximum-ram").getAsString() + "M ");
        // Config Java Arguments
        command.append(config.get("java-arguments").getAsString() + " ");
        // Mojang Tricks
        command.append("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump ");
        // Natives path
        command.append("-Djava.library.path=\"" + Directories.getVersionsDirectory().getPath() + File.separator + pack.getGameVersion() + File.separator + "natives\" ");
        // Classpath
        command.append("-cp \"");
        for (JsonElement jsonElement : FileUtils.readJsonFromFile(Directories.getDirectoryManifest(Directories.getForgeDirectory())).getAsJsonArray("forgeVersions")) {
            if (jsonElement.getAsJsonObject().get("version").getAsString().equals(pack.getForgeVersion()))
                command.append(jsonElement.getAsJsonObject().get("classpathLibraries").getAsString().replace("\\\\", "\\") + ";");
        }
        for (JsonElement jsonElement : FileUtils.readJsonFromFile(Directories.getDirectoryManifest(Directories.getVersionsDirectory())).getAsJsonArray("versions")) {
            if (jsonElement.getAsJsonObject().get("version").getAsString().equals(pack.getGameVersion()))
                command.append(jsonElement.getAsJsonObject().get("classpathLibraries").getAsString().replace("\\\\", "\\") + ";");
        }
        command.append(new File(Directories.getVersionsDirectory().getPath() + File.separator + pack.getGameVersion() + File.separator + pack.getGameVersion() + ".jar").getPath() + "\" ");

        //Loader class
        command.append("net.minecraft.launchwrapper.Launch ");
        // Dimensions
        command.append("--width 960 --height 540 ");
        // Username
        command.append("--username " + account.getUsername() + " ");
        // Version
        command.append("--version " + pack.getForgeVersion() + " ");
        // Game Dir
        command.append("--gameDir \"" + new File(Directories.getInstancesDirectory().getPath() + File.separator + pack.getName().replace(" ", "-")).getPath() + "\" ");
        // Assets Dir
        String assetsVersion = FileUtils.readJsonFromFile(new File(Directories.getVersionsDirectory().getPath() + File.separator + pack.getGameVersion() + File.separator + pack.getGameVersion() + ".json")).get("assets").getAsString();
        command.append("--assetsDir \"" + new File(Directories.getAssetsDirectory().getPath() + File.separator + assetsVersion).toPath() + "\" ");
        // Assets Index
        command.append("--assetsIndex " + assetsVersion + " ");
        // UUID
        command.append("--uuid " + account.getUuid() + " ");
        // Access Token
        command.append("--accessToken " + account.getSession().getAccessToken() + " ");
        // User Type
        command.append("--userType mojang ");
        // Tweak Class
        command.append("--tweakClass net.minecraftforge.fml.common.launcher.FMLTweaker ");
        // Version Type
        command.append("--versionType Forge");

        System.out.println(command.toString());
        try {
            Runtime.getRuntime().exec(command.toString(), null, new File(Directories.getInstancesDirectory().getPath() + File.separator + pack.getName().replace(" ", "-")));
            System.out.println("Game Launched.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
