package net.dirtcraft.dirtlauncher.configuration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.configuration.manifests.ForgeManifest;
import net.dirtcraft.dirtlauncher.configuration.manifests.InstanceManifest;
import net.dirtcraft.dirtlauncher.configuration.manifests.VersionManifest;
import net.dirtcraft.dirtlauncher.data.DirtLauncher.Settings;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;


public final class Config extends ConfigBase<Settings>{
    private ForgeManifest forgeManifest;
    private VersionManifest versionManifest;
    private InstanceManifest instanceManifest;
    private final Path launcherDirectory;
    private final String defaultRuntime;

    public Config(Path launcherDirectory, List<String> options) {
        super(launcherDirectory.resolve("configuration.json").toFile(), Settings.class, ()->new Settings(launcherDirectory));
        this.launcherDirectory = launcherDirectory;
        final String javaExecutable = SystemUtils.IS_OS_WINDOWS ? "javaw" : "java";
        if (options.contains("-installed") || options.contains("-useBundledRuntime")) {
            final Path runtimeDirectory = launcherDirectory.resolve("Runtime");
            defaultRuntime = runtimeDirectory
                    .resolve(options.contains("-x86") ? "jre8_x86" : "jre8_x64")
                    .resolve("bin")
                    .resolve(javaExecutable)
                    .toFile().getPath();
        } else {
            defaultRuntime = javaExecutable;
        }

        if (!configFile.exists()) {
            configBase = tFactory.get();
            initGameDirectory();
            saveAsync();
        } else load();
        forgeManifest = new ForgeManifest(configBase.getForgeDirectory());
        versionManifest = new VersionManifest(configBase.getVersionsDirectory());
        instanceManifest = new InstanceManifest(getInstancesDirectory());
    }

    private void initGameDirectory(){
        File assets = getAssetsDirectory().toFile();
        System.out.println(configBase.getGameDirectory().toFile().mkdirs()?"Successfully created":"Failed to create"+" game directory");
        System.out.println(assets.mkdirs()?"Successfully created":"Failed to create"+" assets directory.");
        // Ensure that the application folders are created
        if(!getDirectoryManifest(assets).exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.add("assets", new JsonArray());
            JsonUtils.writeJsonToFile(getDirectoryManifest(assets), emptyManifest);
        }
    }

    public void updateSettings(int minimumRam, int maximumRam, String javaArguments, String gameDirectory){
        Settings settingsNew = new Settings(minimumRam, maximumRam, javaArguments, gameDirectory);
        Settings settingsOld = configBase;
        configBase = settingsNew;
        if (!settingsOld.getGameDirectoryAsString().equals(gameDirectory)) moveGameDirectories(settingsOld, settingsNew);
        saveAsync();
    }

    private void moveGameDirectories(Settings current, Settings updated){
        try {
            FileUtils.moveDirectory(current.getInstancesDirectory().toFile(), updated.getInstancesDirectory().toFile());
            FileUtils.moveDirectory(current.getVersionsDirectory().toFile(), updated.getVersionsDirectory().toFile());
            FileUtils.moveDirectory(current.getAssetsDirectory().toFile(), updated.getAssetsDirectory().toFile());
            FileUtils.moveDirectory(current.getForgeDirectory().toFile(), updated.getForgeDirectory().toFile());
            instanceManifest = new InstanceManifest(configBase.getInstancesDirectory());
            versionManifest = new VersionManifest(configBase.getVersionsDirectory());
            forgeManifest = new ForgeManifest(configBase.getForgeDirectory());
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    @Override
    public void load(){
        configBase = JsonUtils.parseJson(configFile, type, Settings::migrate, Settings::isValid).orElse(tFactory.get());
        if (configBase.migrated) saveAsync();
    }

    public int getMinimumRam() {
        return configBase.getMinimumRam();
    }

    public int getMaximumRam() {
        return configBase.getMaximumRam();
    }

    public String getJavaArguments() {
        return configBase.getJavaArguments();
    }

    public Path getGameDirectory() {
        return configBase.getGameDirectory();
    }

    public ForgeManifest getForgeManifest(){
        return forgeManifest;
    }

    public VersionManifest getVersionManifest(){
        return versionManifest;
    }

    public InstanceManifest getInstanceManifest(){
        return instanceManifest;
    }

    public Path getLogDirectory() {
        return launcherDirectory.resolve("logs");
    }

    public Path getInstancesDirectory() {
        return configBase.getInstancesDirectory();
    }

    public Path getAssetsDirectory() { // todo: delete
        return configBase.getAssetsDirectory();
    }

    public File getDirectoryManifest(File directory) { // todo: delete
        return directory.toPath().resolve("manifest.json").toFile();
    }

    public String getDefaultRuntime() {
        return defaultRuntime;
    }
}
