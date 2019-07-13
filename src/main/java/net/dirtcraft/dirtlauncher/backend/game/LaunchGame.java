package net.dirtcraft.dirtlauncher.backend.game;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.backend.config.Directories;
import net.dirtcraft.dirtlauncher.backend.objects.Account;
import net.dirtcraft.dirtlauncher.backend.objects.Pack;
import net.dirtcraft.dirtlauncher.backend.utils.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;

public class LaunchGame {

    public static void launchPack(Pack pack, Account account) {
        JsonObject config = FileUtils.readJsonFromFile(Directories.getConfiguration());

        StringBuilder command = new StringBuilder();
        if(SystemUtils.IS_OS_UNIX) {
            command.append("bash -c ");
        }
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

        String launchCommand = command.toString();

        if (SystemUtils.IS_OS_UNIX) launchCommand = launchCommand.replace(";", ":");

        System.out.println(launchCommand);
        try {
            Runtime.getRuntime().exec(launchCommand, null, new File(Directories.getInstancesDirectory().getPath() + File.separator + pack.getName().replace(" ", "-")));
            System.out.println("Game Launched.");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
