package net.dirtcraft.dirtlauncher.backend.game;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.backend.config.Paths;
import net.dirtcraft.dirtlauncher.backend.objects.Account;
import net.dirtcraft.dirtlauncher.backend.objects.Pack;
import net.dirtcraft.dirtlauncher.backend.utils.FileUtils;

import java.io.File;
import java.io.IOException;

public class LaunchGame {

    public static void launchPack(Pack pack, Account account) {
        JsonObject config = FileUtils.readJsonFromFile(Paths.getConfiguration());

        StringBuilder command = new StringBuilder();
        command.append("java ");

        // RAM
        command.append("-Xms" + config.get("minimum-ram").getAsString() + "M -Xmx" + config.get("maximum-ram").getAsString() + "M ");
        // Config Java Arguments
        command.append(config.get("java-arguments").getAsString() + " ");
        // Mojang Tricks
        command.append("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump ");
        // Natives path
        command.append("-Djava.library.path=\"" + Paths.getVersionsDirectory().getPath() + File.separator + pack.getGameVersion() + File.separator + "natives\" ");
        // Classpath
        command.append("-cp \"");
        for (JsonElement jsonElement : FileUtils.readJsonFromFile(Paths.getDirectoryManifest(Paths.getForgeDirectory())).getAsJsonArray("forgeVersions")) {
            if (jsonElement.getAsJsonObject().get("version").getAsString().equals(pack.getForgeVersion()))
                command.append(jsonElement.getAsJsonObject().get("classpathLibraries").getAsString().replace("\\\\", "\\") + ";");
        }
        for (JsonElement jsonElement : FileUtils.readJsonFromFile(Paths.getDirectoryManifest(Paths.getVersionsDirectory())).getAsJsonArray("versions")) {
            if (jsonElement.getAsJsonObject().get("version").getAsString().equals(pack.getGameVersion()))
                command.append(jsonElement.getAsJsonObject().get("classpathLibraries").getAsString().replace("\\\\", "\\") + ";");
        }
        command.append(new File(Paths.getVersionsDirectory().getPath() + File.separator + pack.getGameVersion() + File.separator + pack.getGameVersion() + ".jar").getPath() + "\" ");

        //Loader class
        command.append("net.minecraft.launchwrapper.Launch ");
        // Dimensions
        command.append("--width 960 --height 540 ");
        // Username
        command.append("--username " + account.getUsername() + " ");
        // Version
        command.append("--version " + pack.getForgeVersion() + " ");
        // Game Dir
        command.append("--gameDir \"" + new File(Paths.getInstancesDirectory().getPath() + File.separator + pack.getName().replace(" ", "-")).getPath() + "\" ");
        // Assets Dir
        String assetsVersion = FileUtils.readJsonFromFile(new File(Paths.getVersionsDirectory().getPath() + File.separator + pack.getGameVersion() + File.separator + pack.getGameVersion() + ".json")).get("assets").getAsString();
        command.append("--assetsDir \"" + new File(Paths.getAssetsDirectory().getPath() + File.separator + assetsVersion).toPath() + "\" ");
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
            Process game = Runtime.getRuntime().exec(command.toString());
            System.out.println("Game Launched.");
            System.out.println(game.isAlive());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
