package net.dirtcraft.dirtlauncher.utils;

import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.data.DirtLauncher.Settings;
import net.dirtcraft.dirtlauncher.data.Minecraft.JavaVersion;
import net.dirtcraft.dirtlauncher.game.authentification.Account;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Launcher {
    private final File directory;
    private final JavaVersion java;
    private final Modpack modpack;
    private final List<String> javaArgs = new ArrayList<>();
    private final List<String> gameArgs = new ArrayList<>();
    private final List<String> classpath = new ArrayList<>();

    public Launcher(Modpack modpack, JavaVersion java) {
        this.modpack = modpack;
        this.directory = modpack.getInstanceDirectory();
        this.java = java;
    }

    private String getJavaExec() {
        return Main.getConfig()
                .getJavaDirectory()
                .toPath()
                .resolve(java.getJavaExec())
                .toAbsolutePath()
                .toString();
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

    public String[] processArgs(Settings settings, Account account) {
        ArrayList<String> program = new ArrayList<>();
        program.add(getJavaExec());
        program.add("-Djava.library.path=" + settings.getVersionsDirectory().resolve(modpack.getGameVersion()).resolve("natives"));
        program.addAll(javaArgs);
        program.add("-cp");
        program.add(String.join(";", classpath));
        program.add(String.format("-Xmx%dM", settings.getMaximumRam()));
        program.add(String.format("-Xms%dM", settings.getMinimumRam()));
        program.add("cpw.mods.modlauncher.Launcher");
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
        applyJavaArgs(
                "-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump",
                "-Dos.name=Windows 10",
                "-Dos.version=10.0",
                "-Xss1M",
                "-Dminecraft.launcher.brand=minecraft-launcher",
                "-Dminecraft.launcher.version=2.2.10675",
                "-XX:+IgnoreUnrecognizedVMOptions",
                "--add-exports=java.base/sun.security.util=ALL-UNNAMED",
                "--add-exports=jdk.naming.dns/com.sun.jndi.dns=java.naming",
                "--add-opens=java.base/java.util.jar=ALL-UNNAMED",
                "-XX:+UnlockExperimentalVMOptions",
                "-XX:+UseG1GC",
                "-XX:G1NewSizePercent=20",
                "-XX:G1ReservePercent=20",
                "-XX:MaxGCPauseMillis=50",
                "-XX:G1HeapRegionSize=32M");
        applyGameArgs(
                "--version", "1.16.5-forge-36.2.9",
                "--assetIndex", "1.16",
                "--versionType", "release",
                "--launchTarget", "fmlclient",
                "--fml.forgeVersion", "36.2.9",
                "--fml.mcVersion", "1.16.5",
                "--fml.forgeGroup", "net.minecraftforge",
                "--fml.mcpVersion", "20210115.111550");
        return new ProcessBuilder(processArgs(settings, account)).directory(directory)
                .inheritIO()
                .start();
    }
}
