package net.dirtcraft.dirtlauncher.configuration.manifests;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
        assert configBase != null;
    }

    @Override
    public void load(){
        super.load();
        configBase.values().forEach(entry->entry.setOuterReference(this));
    }

    @Override
    protected Map<String, Entry> migrate(JsonObject jsonObject) {
        Map<String, Entry> entries = tFactory.get();
        for (JsonElement jsonElement : jsonObject.get("assets").getAsJsonArray()) {
            String assetVersion = jsonElement.getAsJsonObject().get("version").getAsString();
            String gameVersion = assetVersion.equalsIgnoreCase("1.12")? "1.12.2" : assetVersion;
            entries.put(gameVersion, new Entry(assetVersion, this));
        }
        return entries;
    }

    public void add(String gameVersion, String assetVersion){
        configBase.put(gameVersion, new Entry(assetVersion, this));
    }

    public void add(String gameVersion, Entry asset){
        configBase.put(gameVersion, asset);
    }

    public boolean isInstalled(String gameVersion){
        return configBase.containsKey(gameVersion);
    }

    public Optional<Entry> getViaAssetIndex(String assetVersion){
        return configBase.values().stream()
                .filter(entry -> entry.assetVersion.equalsIgnoreCase(assetVersion))
                .findFirst();
    }

    public Optional<Entry> get(String gameVersion){
        return Optional.ofNullable(configBase.get(gameVersion));
    }

    public static class Entry {
        private final String assetVersion;
        private transient AssetManifest outerReference;

        public Entry(String gameVersion, AssetManifest outerReference){
            this.assetVersion = gameVersion;
            this.outerReference = outerReference;
        }

        private boolean isValid(){
            return assetVersion != null;
        }

        public Path getAssetDirectory(){
            return getOuterReference().directory;
        }

        private AssetManifest getOuterReference() {
            return outerReference;
        }

        private void setOuterReference(AssetManifest outerReference) {
            this.outerReference = outerReference;
        }
    }
}
