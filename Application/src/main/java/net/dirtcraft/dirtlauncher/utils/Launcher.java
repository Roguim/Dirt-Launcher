package net.dirtcraft.dirtlauncher.utils;

import net.dirtcraft.dirtlauncher.DirtLauncher;
import net.dirtcraft.dirtlauncher.configuration.Settings;
import net.dirtcraft.dirtlauncher.game.authentification.Account;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.lib.data.json.forge.ForgeVersion;
import net.dirtcraft.dirtlauncher.lib.data.json.mojang.Java.JavaVersion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Launcher {
    private final File directory;
    private final JavaVersion java;
    private final Modpack modpack;
    private String mainClass;
    private final List<String> javaArgs = new ArrayList<>();
    private final List<String> gameArgs = new ArrayList<>();
    private final List<String> classpath = new ArrayList<>();

    public Launcher(Modpack modpack, JavaVersion java) {
        this.modpack = modpack;
        this.directory = modpack.getInstanceDirectory();
        this.java = java;
    }

    public Launcher applyJavaArgs(String... args) {
        javaArgs.addAll(Arrays.asList(args));
        return this;
    }

    public Launcher applyGameArgs(String... args) {
        gameArgs.addAll(Arrays.asList(args));
        return this;
    }

    public Launcher applyClasspath(String... args) {
        classpath.addAll(Arrays.asList(args));
        return this;
    }

    public Launcher applyForgeProfile(ForgeVersion forge) {
        this.mainClass = forge.getMainClass();
        applyJavaArgs(forge.getRuntimeArgs());
        applyGameArgs(forge.getMinecraftArgs());
        applyGameArgs(
                "--version", forge.getForgeVersion(),
                "--assetIndex", forge.getMinecraftVersion(),
                "--versionType", forge.getReleaseType());
        return this;
    }

    public String[] processArgs(Settings settings, Account account) {
        ArrayList<String> program = new ArrayList<>();
        program.add(java.getJavaExec().toString());
        program.add("-Djava.library.path=" + settings.getVersionsDirectory().resolve(modpack.getGameVersion()).resolve("natives"));
        program.addAll(javaArgs);
        program.add(String.format("-Xmx%dM", settings.getMaximumRam()));
        program.add(String.format("-Xms%dM", settings.getMinimumRam()));
        program.add("-cp");
        program.add(String.join(";", classpath));
        program.add(mainClass);
        program.addAll(gameArgs);
        program.addAll(Arrays.asList(
                "--assetsDir", settings.getAssetsDirectory().toString(),
                "--gameDir", directory.toString(),
                "--username", account.getAlias(),
                "--uuid", account.getId(),
                "--accessToken", account.getAccessToken(),
                "--userType", "msa"
        ));
        return program.toArray(new String[0]);
    }
    public Process launch(Settings settings, Account account) throws IOException {
        if (mainClass == null) throw new IOException("A game-version profile has not been applied!");
        applyJavaArgs(
                "-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump",
                "-Dos.name=Windows 10",
                "-Dos.version=10.0",
                "-Xss1M",
                "-Dminecraft.launcher.brand=minecraft-launcher",
                "-Dminecraft.launcher.version=2.2.10675",
                "-XX:+UnlockExperimentalVMOptions",
                "-XX:+UseG1GC",
                "-XX:G1NewSizePercent=20",
                "-XX:G1ReservePercent=20",
                "-XX:MaxGCPauseMillis=50",
                "-XX:G1HeapRegionSize=32M");
        return new ProcessBuilder(processArgs(settings, account))
                .directory(directory)
                //.inheritIO()
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectInput(ProcessBuilder.Redirect.INHERIT)
                .start();
    }
}
