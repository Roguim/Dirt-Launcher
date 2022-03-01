package net.dirtcraft.dirtlauncher.lib.data.json.mojang.Java;

import net.dirtcraft.dirtlauncher.lib.config.Constants;
import net.dirtcraft.dirtlauncher.lib.data.tasks.DownloadTask;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

public class JavaVersion {
    public static final JavaVersion DEFAULT = new JavaVersion("java-runtime-beta", 17);
    public static final JavaVersion LEGACY = new JavaVersion("jre-legacy", 8);
    public final String component;
    public final int majorVersion;
    public transient Path folder;
    public transient File javaw;
    public transient File java;

    public JavaVersion(String component, int majorVersion){
        this.component = component;
        this.majorVersion = majorVersion;
    }

    public Path getFolder(){
        return folder == null? folder = Constants.DIR_RUNTIMES.resolve(String.format("%s (%d)", component, majorVersion)) : folder;
    }

    public File getJavaWExec() {
        return javaw == null? javaw = getFolder().resolve("bin").resolve("javaw").toFile() : javaw;
    }

    public File getJavaExec() {
        return java == null? java = getFolder().resolve("bin").resolve("java").toFile() : java;
    }

    public boolean isInstalled() {
        return getJavaExec().getParentFile().exists();
    }

    @SuppressWarnings("unchecked")
    public Collection<DownloadTask> getDownloads() {
        return JavaManifest.getManifest()
                .flatMap(x->x.getVersionManifest(component))
                .map(x->x.getDownloads(getFolder().toFile()))
                .orElse(Collections.EMPTY_LIST);
    }
}
