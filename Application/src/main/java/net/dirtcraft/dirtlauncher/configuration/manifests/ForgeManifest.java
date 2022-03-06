package net.dirtcraft.dirtlauncher.configuration.manifests;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.configuration.ManifestBase;
import net.dirtcraft.dirtlauncher.lib.data.json.forge.ForgeVersion;
import net.dirtcraft.dirtlauncher.lib.data.tasks.JsonTask;
import net.dirtcraft.dirtlauncher.logging.Logger;
import net.dirtcraft.dirtlauncher.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ForgeManifest extends ManifestBase<Map<String, ForgeManifest.Entry>> {
    @SuppressWarnings("UnstableApiUsage")
    public ForgeManifest(Path dir) {
        super(dir, new TypeToken<Map<String, Entry>>(){}, HashMap::new);
        load();
        configBase.values().forEach(entry->entry.outerReference = this);
        assert configBase != null;
    }

    private String getMinecraftVersion(File forgeJar, String forgeVersion){
        String escapedVersion = forgeVersion.replaceAll("[.]", "[.]");
        String jar = forgeJar.toString().replace(forgeJar.getParent(), "");
        System.out.println(jar);
        return jar.replaceAll("^.*forge-(\\d+\\.\\d+\\.\\d+)-" + escapedVersion + "-universal.jar$", "$1");
    }

    @Override
    public void load(){
        super.load();
        configBase.values().forEach(entry->entry.setOuterReference(this));
    }

    @Override
    protected Map<String, Entry> migrate(JsonObject jsonObject) {
        Map<String, Entry> map = new HashMap<>();
        try {
            StreamSupport.stream(jsonObject.getAsJsonArray("forgeVersions").spliterator(), false)
                    .map(JsonElement::getAsJsonObject)
                    .forEach(forgeManifest -> {//14.23.5.2847
                        final String[] classpathLibraries = forgeManifest.get("classpathLibraries").getAsString().split(";");
                        final String forgeVersion = forgeManifest.get("version").getAsString();
                        final String minecraftVersion = getMinecraftVersion(new File(classpathLibraries[0]), forgeVersion);
                        final Entry entry = new Entry(minecraftVersion, forgeVersion, this);
                        final Path libDir = entry.getLibsFolder();
                        Arrays.stream(classpathLibraries)
                                .filter(lib->!classpathLibraries[0].equalsIgnoreCase(lib))
                                .map(Paths::get)
                                .map(libDir::relativize)
                                .map(Path::toString)
                                .forEach(entry.libraries::add);
                        entry.libraries.sort(String::compareTo);
                        map.put(forgeVersion, entry);
                    });
        } catch (Exception e){
            Logger.INSTANCE.error(e);
        }
        return map;
    }

    public Optional<Entry> get(String forgeVersion){
        if (!isInstalled(forgeVersion)) return Optional.empty();
        else return Optional.of(configBase.get(forgeVersion));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Entry create(String minecraftVersion, String forgeVersion) throws IOException {
        Entry entry = new Entry(minecraftVersion, forgeVersion, this);
        configBase.put(forgeVersion, entry);
        FileUtils.deleteDirectory(entry.getForgeFolder().toFile());
        entry.getLibsFolder().toFile().mkdirs();
        entry.getForgeFolder().toFile().mkdirs();
        entry.getTempFolder().toFile().mkdirs();
        return entry;
    }

    public boolean isInstalled(String forgeVersion) {
        final Entry entry = configBase.get(forgeVersion);
        if (entry == null) return false;
        if (!entry.getForgeFolder().toFile().exists()){
            configBase.remove(forgeVersion);
            saveAsync();
            return false;
        } else return true;
    }

    public static class Entry {
        private transient ForgeManifest outerReference;
        final int manifestVersion = 1;
        final String forgeVersion;
        final String minecraftVersion;
        final ArrayList<String> libraries;
        public Entry(String minecraftVersion, String forgeVersion, ForgeManifest outerReference){
            this.minecraftVersion = minecraftVersion;
            this.forgeVersion = forgeVersion;
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
            return getForgeFolder().resolve("libraries");
        }

        public File getForgeManifestFile(){
            return new File(getForgeFolder().toFile(), forgeVersion + ".json");
        }

        public ForgeVersion getForgeManifest() throws IOException {
            try {
                return ForgeVersion.fromFile(minecraftVersion, getForgeManifestFile()).runOrThrow();
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        public File getForgeJarFile(){
            return new File(getForgeFolder().toFile(), getForgeJarFilename());
        }

        public String getForgeJarFilename(){
            return "forge-" + minecraftVersion + "-" + forgeVersion + "-universal.jar";
        }

        public Path getTempFolder(){
            return getForgeFolder().resolve("temp");
        }

        public Path getForgeFolder(){
            return getOuterReference().directory.resolve(forgeVersion);
        }


        public void saveAsync(){
            getOuterReference().saveAsync();
        }

        private ForgeManifest getOuterReference(){
            return outerReference;
        }

        private void setOuterReference(ForgeManifest instance){
            outerReference = instance;
        }
    }
}
