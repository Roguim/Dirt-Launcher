package net.dirtcraft.dirtlauncher.configuration.manifests;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.configuration.ManifestBase;
import net.dirtcraft.dirtlauncher.logging.Logger;
import net.dirtcraft.dirtlauncher.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class VersionManifest extends ManifestBase<Map<String, VersionManifest.Entry>> {
    @SuppressWarnings("UnstableApiUsage")
    public VersionManifest(Path dir) {
        super(dir, new TypeToken<Map<String, Entry>>(){}, HashMap::new);
        load();
        configBase.values().forEach(entry->entry.outerReference = this);
        assert configBase != null;
    }

    @Override
    protected Map<String, Entry> migrate(JsonObject jsonObject) {
        Map<String, Entry> map = new HashMap<>();
        try {
            StreamSupport.stream(jsonObject.getAsJsonArray("versions").spliterator(), false)
                    .map(JsonElement::getAsJsonObject)
                    .forEach(versionManifest -> {
                        final String version = versionManifest.get("version").getAsString();
                        final Entry entry = new Entry(version, this);
                        final Path libDir = entry.getLibsFolder();
                        Arrays.stream(versionManifest.get("classpathLibraries").getAsString().split(";"))
                                .map(Paths::get)
                                .map(libDir::relativize)
                                .map(Path::toString)
                                .forEach(entry.libraries::add);
                        entry.libraries.sort(String::compareTo);
                        map.put(version, entry);
                    });
        } catch (Exception e){
            Logger.INSTANCE.error(e);
        }
        return map;
    }

    public Optional<Entry> get(String version){
        if (!isInstalled(version)) return Optional.empty();
        else return Optional.of(configBase.get(version));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Entry create(String version) throws IOException {
            Entry entry = new Entry(version, this);
            configBase.put(version, entry);
            FileUtils.deleteDirectory(entry.getVersionFolder().toFile());
            entry.getNativesFolder().toFile().mkdirs();
            entry.getLibsFolder().toFile().mkdirs();
            entry.getVersionFolder().toFile().mkdirs();
            return entry;
    }

    public boolean isInstalled(String minecraftVersion) {
            return configBase.containsKey(minecraftVersion);
    }

    public static class Entry {
        private transient VersionManifest outerReference;
        final int manifestVersion = 1;
        final String gameVersion;
        final ArrayList<String> libraries;
        public Entry(String version, VersionManifest outerReference){
            this.gameVersion = version;
            libraries = new ArrayList<>();
            this.outerReference = outerReference;
        }

        public String getLibs(){
            final Path libDir = getLibsFolder();
            return libraries.stream()
                    .map(libDir::resolve)
                    .map(Path::toString)
                    .collect(Collectors.joining(";"));
        }

        public void addLibs(Collection<File> files){
            final Path libDir = getLibsFolder();
            files.stream()
                    .map(File::toPath)
                    .map(libDir::relativize)
                    .map(Path::toString)
                    .forEach(libraries::add);
            libraries.sort(String::compareTo);
        }

        public Path getLibsFolder(){
            return getVersionFolder().resolve("libraries");
        }

        public Path getNativesFolder(){
            return getVersionFolder().resolve("natives");
        }

        public File getVersionManifestFile(){
            return new File(getVersionFolder().toFile(), gameVersion + ".json");
        }

        public File getVersionJarFile(){
            return new File(getVersionFolder().toFile(), gameVersion + ".jar");
        }

        public Path getVersionFolder(){
            return getMain().directory.resolve(gameVersion);
        }

        public void saveAsync(){
            getMain().saveAsync();
        }

        private VersionManifest getMain(){
            return outerReference;
        }
    }
}
