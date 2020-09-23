package net.dirtcraft.dirtlauncher.game.installation.manifests;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.logging.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class VersionManifest extends InstallationManifest<Multimap<String, String>> {
    final Path parent;
    @SuppressWarnings("UnstableApiUsage")
    public VersionManifest() {
        super(Main.getConfig().getDirectoryManifest(Main.getConfig().getVersionsDirectory()), new TypeToken<Multimap<String, String>>(){}, ArrayListMultimap::create);
        parent = path.getParentFile().toPath();
        load();
    }

    @Override
    protected Multimap<String, String> migrate(JsonObject jsonObject) {
        Multimap<String, String> multimap = ArrayListMultimap.create();
        try {
            StreamSupport.stream(jsonObject.getAsJsonArray("versions").spliterator(), false)
                    .map(JsonElement::getAsJsonObject)
                    .forEach(versionManifest -> {
                        final String version = versionManifest.get("version").getAsString();
                        final List<String> libraries = Arrays.stream(versionManifest.get("classpathLibraries").getAsString().split(";"))
                                .map(Paths::get)
                                .map(parent::relativize)
                                .map(Path::toString)
                                .collect(Collectors.toList());
                        multimap.putAll(version, libraries);
                    });
        } catch (Exception e){
            Logger.INSTANCE.error(e);
        }
        return multimap;
    }

    public Optional<String> getLibs(String version){
        if (!configBase.containsKey(version)) return Optional.empty();
        String result = configBase.get(version).stream()
                .map(parent::resolve)
                .map(Path::toString)
                .collect(Collectors.joining(";"));
        return Optional.of(result);
    }

    public void addLibs(String version, Collection<File> files){
        files.stream()
                .map(File::toPath)
                .map(parent::relativize)
                .map(Path::toString)
                .forEach(s->configBase.put(version, s));
    }

    public boolean isInstalled(String minecraftVersion){
        return configBase.containsKey(minecraftVersion);
    }
}
