package net.dirtcraft.dirtlauncher.backend.game;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.stage.Stage;
import net.dirtcraft.dirtlauncher.Controllers.Install;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.backend.config.Directories;
import net.dirtcraft.dirtlauncher.backend.config.Internal;
import net.dirtcraft.dirtlauncher.backend.objects.Account;
import net.dirtcraft.dirtlauncher.backend.objects.Pack;
import net.dirtcraft.dirtlauncher.backend.utils.FileUtils;
import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class LaunchGame {

    private static Logger logger = LogManager.getLogger("Launch Game");

    public static void launchPack(Pack pack, Account account) {
        JsonObject config = FileUtils.readJsonFromFile(Directories.getConfiguration());

        StringBuilder command = new StringBuilder();
        command.append("java");
        if (SystemUtils.IS_OS_WINDOWS) command.append("w");
        command.append(" ");

        // RAM
        command.append("-Xms" + config.get("minimum-ram").getAsString() + "M -Xmx" + config.get("maximum-ram").getAsString() + "M ");

        // Configurable Java Arguments
        String javaArgs = config.get("java-arguments").getAsString();
        if (MiscUtils.isEmptyOrNull(javaArgs)) command.append(Internal.DEFAULT_JAVA_ARGS);
        else command.append(javaArgs);
        command.append(" ");

        // Language Tricks
        command.append("-Dfml.ignorePatchDiscrepancies=true -Dfml.ignoreInvalidMinecraftCertificates=true -Duser.language=en -Duser.country=US ");

        // Mojang Tricks
        command.append("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump ");
        // Natives path
        String nativesPath = Directories.getVersionsDirectory().getPath() + File.separator + pack.getGameVersion() + File.separator + "natives";
        command.append("-Djava.library.path=\"" + nativesPath + "\" ");
        command.append("-Dorg.lwjgl.librarypath=\"" + nativesPath + "\" ");
        command.append("-Dnet.java.games.input.librarypath=\"" + nativesPath + "\" ");
        command.append("-Duser.home=\"" + new File(Directories.getInstancesDirectory().getPath() + File.separator + pack.getName().replace(" ", "-")).getPath() + "\" ");
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
        command.append("--username=" + account.getUsername() + " ");
        // Version
        command.append("--version=" + pack.getForgeVersion() + " ");
        // Game Dir
        command.append("--gameDir=\"" + new File(Directories.getInstancesDirectory().getPath() + File.separator + pack.getName().replace(" ", "-")).getPath() + "\" ");
        // Assets Dir
        String assetsVersion = FileUtils.readJsonFromFile(new File(Directories.getVersionsDirectory().getPath() + File.separator + pack.getGameVersion() + File.separator + pack.getGameVersion() + ".json")).get("assets").getAsString();
        command.append("--assetsDir=\"" + new File(Directories.getAssetsDirectory().getPath() + File.separator + assetsVersion).toPath() + "\" ");
        // Assets Index
        command.append("--assetsIndex=" + assetsVersion + " ");
        // UUID
        command.append("--uuid=" + account.getUuid() + " ");
        // Access Token
        command.append("--accessToken=" + account.getSession().getAccessToken() + " ");
        // User Type
        command.append("--userType=mojang ");
        // Tweak Class
        command.append("--tweakClass=net.minecraftforge.fml.common.launcher.FMLTweaker ");
        // Version Type
        command.append("--versionType=Forge");

        String launchCommand = command.toString();

        if (SystemUtils.IS_OS_UNIX) launchCommand = launchCommand.replace(";", ":");

        System.out.println(launchCommand);
        String finalLaunchCommand = launchCommand;
        new Thread(() -> {
            try {
                Process process;
                if (!SystemUtils.IS_OS_UNIX) process = Runtime.getRuntime().exec(finalLaunchCommand);
                else process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", finalLaunchCommand});

                Platform.runLater(() -> {
                    //Close install stage if it's open
                    Install.getStage().ifPresent(Stage::close);

                    //Minimize the main stage to the task bar
                    Main.getInstance().getStage().setIconified(true);
                });

                String line;
                BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                Main.getLogger().warn("Starting Minecraft Logger...");
                while ((line = input.readLine()) != null) {
                    logger.info(line);
                }
                input.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }).start();
    }

}