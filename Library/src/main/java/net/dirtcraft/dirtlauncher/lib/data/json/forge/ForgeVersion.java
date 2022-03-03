package net.dirtcraft.dirtlauncher.lib.data.json.forge;

import net.dirtcraft.dirtlauncher.lib.data.tasks.FileTask;
import net.dirtcraft.dirtlauncher.lib.data.tasks.JsonTask;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

public interface ForgeVersion {
    String getFileName();
    Stream<FileTask> getClientLibraries(File folder, ZipFile jar);
    Optional<ForgeInstallManifest> getPostProcess(ZipFile jar);
    String getMainClass();
    String getReleaseType();
    String getForgeVersion();
    String getMinecraftVersion();
    String[] getMinecraftArgs();
    String[] getRuntimeArgs();

    static JsonTask<? extends ForgeVersion> fromInstaller(String version, ZipFile jar) {
        if ("1.7.10".equals(version)) return new JsonTask<>(jar, "install_profile.json", LegacyForgeManifest.class);
        else return new JsonTask<>(jar, "version.json", ForgeRunProfile.class);
    }
}
