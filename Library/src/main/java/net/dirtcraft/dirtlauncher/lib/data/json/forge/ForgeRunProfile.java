package net.dirtcraft.dirtlauncher.lib.data.json.forge;

import net.dirtcraft.dirtlauncher.lib.data.json.mojang.Download;
import net.dirtcraft.dirtlauncher.lib.data.json.mojang.Library;
import net.dirtcraft.dirtlauncher.lib.data.tasks.ExtractTask;
import net.dirtcraft.dirtlauncher.lib.data.tasks.FileTask;
import net.dirtcraft.dirtlauncher.lib.data.tasks.JsonTask;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

public class ForgeRunProfile implements ForgeVersion{
    String id;
    String type;
    String mainClass;
    String inheritsFrom;
    Arguments arguments;
    Library[] libraries;
    transient ForgeInstallManifest manifest;

    @Override
    public String getFileName() {
        return "version.json";
    }

    @Override
    public Stream<FileTask> getClientLibraries(File folder, ZipFile jar) {
        return Stream.of(libraries)
                .filter(Library::isRequired)
                .map(lib->{
                    Download artifact = lib.getArtifact().orElse(null);
                    if (artifact == null) return null;
                    if (artifact.url == null || artifact.url.isEmpty()) {
                        return new ExtractTask(jar, "maven/" + artifact.path, new File(folder, artifact.path));
                    } else {
                        return artifact.getDownload(folder);
                    }
                });
    }

    @Override
    public Optional<String> getPostProcess(ZipFile jar) {
        if (manifest == null) manifest = new JsonTask<>(installerJar, "install_profile.json", ForgeInstallManifest.class).run();
        return Optional.of("install_profile.json");
    }

    @Override
    public String[] getMinecraftArgs() {
        return arguments == null || arguments.game == null ? new String[0] : arguments.game;
    }

    @Override
    public String[] getRuntimeArgs() {
        return arguments == null || arguments.jvm == null ? new String[0] : arguments.jvm;
    }

    @Override
    public String getMainClass() {
        return mainClass == null? null : mainClass;
    }

    @Override
    public String getReleaseType() {
        return type == null? null : type;
    }

    @Override
    public String getForgeVersion() {
        return id == null? null : id;
    }

    @Override
    public String getMinecraftVersion() {
        return inheritsFrom == null? null : inheritsFrom;
    }

    public static class Arguments {
        String[] game;
        String[] jvm;
    }
}
