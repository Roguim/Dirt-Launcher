package net.dirtcraft.dirtlauncher.configuration.manifests;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.configuration.ManifestBase;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AssetManifest extends ManifestBase<Map<String, AssetManifest.Entry>> {
    @SuppressWarnings("UnstableApiUsage")
    public AssetManifest(Path directory) {
        super(directory, new TypeToken<Map<String, Entry>>(){}, HashMap::new);
        load();
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    protected Map<String, Entry> migrate(JsonObject jsonObject) {
        Map<String, Entry> entries = tFactory.get();
        Gson gson = Main.gson;
        for (JsonElement jsonElement : jsonObject.get("assets").getAsJsonArray()) {
            Entry entry = gson.fromJson(jsonElement, Entry.class);
            entries.put(entry.version, entry);
        }
        return entries;
    }

    public void add(String gameVersion){
        configBase.put(gameVersion, new Entry(gameVersion));
    }

    public void remove(String gameVersion){
        configBase.remove(gameVersion);
    }

    public boolean isInstalled(String gameVersion){
        return configBase.containsKey(gameVersion);
    }

    public Optional<Entry> get(String gameVersion){
        return Optional.ofNullable(configBase.get(gameVersion));
    }

    public static class Entry {
        private final String version;

        public Entry(String gameVersion){
            this.version = gameVersion;
        }

        private boolean isValid(){
            return version != null;
        }

        public Path getAssetDirectory(){
            return Main.getConfig().getAssetManifest().directory; //todo fix
        }
    }
}
