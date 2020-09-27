package net.dirtcraft.dirtlauncher.configuration;

import net.dirtcraft.dirtlauncher.configuration.manifests.AssetManifest;
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


public final class ConfigurationManager extends ConfigBase<Settings>{
    private ForgeManifest forgeManifest;
    private AssetManifest assetManifest;
    private VersionManifest versionManifest;
    private InstanceManifest instanceManifest;
    private final String defaultRuntime;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public ConfigurationManager(Path launcherDirectory, List<String> options) {
        super(launcherDirectory.resolve("settings.json").toFile(), Settings.class, ()->new Settings(launcherDirectory));
        final File old = launcherDirectory.resolve("configuration.json").toFile();
        if (old.exists()) old.renameTo(configFile);
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
            saveAsync();
        } else load();
        forgeManifest = new ForgeManifest(configBase.getForgeDirectory());
        assetManifest = new AssetManifest(configBase.getAssetsDirectory());
        versionManifest = new VersionManifest(configBase.getVersionsDirectory());
        instanceManifest = new InstanceManifest(configBase.getInstancesDirectory());
    }

    public void updateSettings(int minimumRam, int maximumRam, String javaArguments, String gameDirectory){
        Settings settingsNew = new Settings(minimumRam, maximumRam, javaArguments, gameDirectory);
        Settings settingsOld = configBase;
        moveGameDirectories(settingsOld, settingsNew);
        configBase = settingsNew;
        saveAsync();
    }

    private void moveGameDirectories(Settings current, Settings updated){
        if (current.getGameDirectoryAsString().equals(updated.getGameDirectoryAsString())) return;
        try {
            FileUtils.moveDirectory(current.getInstancesDirectory().toFile(), updated.getInstancesDirectory().toFile());
            FileUtils.moveDirectory(current.getVersionsDirectory().toFile(), updated.getVersionsDirectory().toFile());
            FileUtils.moveDirectory(current.getAssetsDirectory().toFile(), updated.getAssetsDirectory().toFile());
            FileUtils.moveDirectory(current.getForgeDirectory().toFile(), updated.getForgeDirectory().toFile());
            instanceManifest = new InstanceManifest(configBase.getInstancesDirectory());
            versionManifest = new VersionManifest(configBase.getVersionsDirectory());
            assetManifest = new AssetManifest(configBase.getAssetsDirectory());
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

    public Settings getSettings(){
        return configBase;
    }

    public ForgeManifest getForgeManifest(){
        return forgeManifest;
    }

    public AssetManifest getAssetManifest(){
        return assetManifest;
    }

    public VersionManifest getVersionManifest(){
        return versionManifest;
    }

    public InstanceManifest getInstanceManifest(){
        return instanceManifest;
    }

    public String getDefaultRuntime() {
        return defaultRuntime;
    }
}
