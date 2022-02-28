package net.dirtcraft.dirtlauncher.utils;

import net.dirtcraft.dirtlauncher.DirtLauncher;
import net.dirtcraft.dirtlauncher.configuration.ConfigurationManager;
import net.dirtcraft.dirtlauncher.configuration.Constants;
import net.dirtcraft.dirtlauncher.configuration.manifests.AssetManifest;
import net.dirtcraft.dirtlauncher.configuration.manifests.ForgeManifest;
import net.dirtcraft.dirtlauncher.configuration.manifests.InstanceManifest;
import net.dirtcraft.dirtlauncher.configuration.manifests.VersionManifest;
import net.dirtcraft.dirtlauncher.configuration.Settings;
import net.dirtcraft.dirtlauncher.game.authentification.Account;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.lib.data.json.mojang.Java.JavaVersion;
import net.dirtcraft.dirtlauncher.logging.Logger;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LegacyLauncher {
    private final File directory;
    private final JavaVersion java;
    private final Modpack modpack;

    public LegacyLauncher(Modpack modpack, JavaVersion java) {
        this.modpack = modpack;
        this.directory = modpack.getInstanceDirectory();
        this.java = java;
    }


    public Process launchPack(Settings settings, Account session) throws IOException {
        if (session == null) return null;
        ConfigurationManager configManager = DirtLauncher.getConfig();
        InstanceManifest.Entry instanceManifest = configManager.getInstanceManifest().get(modpack).orElseThrow(() -> new IOException("Instance manifest entry not present."));
        VersionManifest.Entry versionManifest = configManager.getVersionManifest().get(modpack.getGameVersion()).orElseThrow(() -> new IOException("Version manifest entry not present."));
        ForgeManifest.Entry forgeManifest = configManager.getForgeManifest().get(modpack.getForgeVersion()).orElseThrow(() -> new IOException("Forge manifest entry not present."));
        AssetManifest.Entry assetManifest = configManager.getAssetManifest().get(modpack.getGameVersion()).orElseThrow(() -> new IOException("Asset manifest entry not present."));
        JavaVersion version = versionManifest.getJavaVersion();
        if (version == null) version = JavaVersion.LEGACY; //for old installs
        File javaDir = version.getFolder().toFile();
        final File instanceDirectory = instanceManifest.getDirectory().toFile();

        List<String> args = new ArrayList<>();
        String javaExecutable = SystemUtils.IS_OS_WINDOWS && !Constants.VERBOSE ? "javaw" : "java";
        String jvm = javaDir.toPath().resolve("bin").resolve(javaExecutable).toString();
        args.add(jvm);

        //args.add(configManager.getDefaultRuntime());

        // RAM
        args.add("-Xms" + settings.getMinimumRam() + "M");
        args.add("-Xmx" + settings.getMaximumRam() + "M");

        // Configurable Java Arguments
        String javaArgs = settings.getJavaArguments();
        if (MiscUtils.isEmptyOrNull(javaArgs)) args.addAll(Arrays.asList(Constants.DEFAULT_JAVA_ARGS.split(" ")));
        else args.addAll(Arrays.asList(javaArgs.split(" ")));

        // Language Tricks
        args.add("-Dfml.ignorePatchDiscrepancies=true");
        args.add("-Dfml.ignoreInvalidMinecraftCertificates=true");
        args.add("-Duser.language=en");
        args.add("-Duser.country=US");

        // Mojang Tricks
        args.add("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");

        // Natives path
        String nativesPath = versionManifest.getNativesFolder().toString();
        args.add("-Djava.library.path=" + nativesPath);
        args.add("-Dorg.lwjgl.librarypath=" + nativesPath);
        args.add("-Dnet.java.games.input.librarypath=" + nativesPath);
        args.add("-Duser.home=" + instanceDirectory.getPath());

        // Classpath
        args.add("-cp");
        args.add(getLibs(versionManifest, forgeManifest));

        //Loader class
        args.add("net.minecraft.launchwrapper.Launch");

        // User Properties
        if (modpack.getGameVersion().equalsIgnoreCase("1.7.10")) {
            args.add("--userProperties");
            args.add("{}");
        }
        // Username
        args.add("--username");
        args.add(session.getAlias());

        // Version
        args.add("--version");
        args.add(modpack.getForgeVersion());

        // Game Dir
        args.add("--gameDir");
        args.add(instanceDirectory.getPath());

        // Assets Dir
        args.add("--assetsDir");
        args.add(assetManifest.getAssetDirectory().toString());

        // Assets Index
        File assetsVersionJsonFile = versionManifest.getVersionManifestFile();
        String assetsVersion = JsonUtils.readJsonFromFile(assetsVersionJsonFile).get("assets").getAsString();
        args.add("--assetIndex");
        args.add(assetsVersion);
        // UUID
        args.add("--uuid");
        args.add(session.getId().toString().replace("-", ""));
        // Access Token
        args.add("--accessToken");
        args.add(session.getAccessToken());

        // User Type
        args.add("--userType");
        args.add("mojang");
        // Tweak Class
        args.add("--tweakClass");
        args.add(!modpack.getGameVersion().equals("1.7.10") ?
                "net.minecraftforge.fml.common.launcher.FMLTweaker" :
                "cpw.mods.fml.common.launcher.FMLTweaker");

        // Version Type
        args.add("--versionType");
        args.add("Forge");

        Logger.INSTANCE.verbose("---DIR---");
        Logger.INSTANCE.verbose(instanceDirectory);
        Logger.INSTANCE.verbose("---ARG---");
        Logger.INSTANCE.verbose(args);
        Logger.INSTANCE.verbose("---END---");

        return new ProcessBuilder()
                .directory(instanceDirectory)
                //.inheritIO()
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectInput(ProcessBuilder.Redirect.INHERIT)
                .command(args)
                .start();
    }

    private String getLibs(VersionManifest.Entry versionManifest, ForgeManifest.Entry forgeManifest) {
        char sep = SystemUtils.IS_OS_UNIX? ':' : ';';
        return String.valueOf(forgeManifest.getForgeJarFile()) + sep +
                forgeManifest.getLibs() + sep +
                versionManifest.getLibs() + sep +
                versionManifest.getVersionJarFile();
    }



}
