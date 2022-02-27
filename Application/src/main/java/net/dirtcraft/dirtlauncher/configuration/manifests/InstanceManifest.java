package net.dirtcraft.dirtlauncher.configuration.manifests;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.configuration.ManifestBase;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class InstanceManifest extends ManifestBase<ArrayList<InstanceManifest.Entry>> {

    public InstanceManifest(Path dir){
        super(dir, new TypeToken<ArrayList<Entry>>(){}, ArrayList::new);
        load();
        assert configBase != null;
    }

    public Optional<Entry> get(Modpack modpack){
        return get(modpack.getName());
    }

    public Optional<Entry> get(String modpack){
        return stream().filter(entry->entry.name.equals(modpack)).findFirst();
    }

    @Override
    public void load(){
        super.load();
        configBase.forEach(entry->entry.setOuterReference(this));
    }

    @Override
    protected ArrayList<Entry> migrate(JsonObject jsonObject) {
        ArrayList<Entry> packs = new ArrayList<>();
        for (JsonElement jsonElement : jsonObject.getAsJsonArray("packs")) {
            JsonObject entry = jsonElement.getAsJsonObject();
            final String name = entry.get("name").getAsString();
            final String version = entry.get("version").getAsString();
            final String gameVersion = entry.get("gameVersion").getAsString();
            final String forgeVersion = entry.get("forgeVersion").getAsString();
            packs.add(new Entry(name, version, gameVersion, forgeVersion, this));
        }
        return packs;
    }

    public Stream<Entry> stream(){
        return configBase.stream();
    }

    public void remove(Modpack modpack){
        Iterator<Entry> modpackIterator = configBase.listIterator();
        while (modpackIterator.hasNext()){
            if (!modpackIterator.next().name.equals(modpack.getName())) continue;
            modpackIterator.remove();
            break;
        }
        saveAsync();
    }

    public void update(Modpack pack){
        InstanceManifest.Entry updated = new InstanceManifest.Entry(pack.getName(), pack.getVersion(), pack.getGameVersion(), pack.getForgeVersion(), this);
        ListIterator<Entry> modpackIterator = configBase.listIterator();
        while (true) {
            if (!modpackIterator.hasNext()) modpackIterator.add(updated);
            else if (modpackIterator.next().name.equals(pack.getName())) modpackIterator.set(updated);
            else continue;
            break;
        }
        saveAsync();
    }

    public static class Entry{
        public final String name;
        public final String version;
        public final String gameVersion;
        public final String forgeVersion;
        private transient InstanceManifest outerReference;

        public Entry(String name, String version, String gameVersion, String forgeVersion, InstanceManifest outerReference){
            this.name = name;
            this.version = version;
            this.gameVersion = gameVersion;
            this.forgeVersion = forgeVersion;
            this.outerReference = outerReference;
        }

        public String getFormattedName() {
            return name.replaceAll("\\s+", "-");
        }

        public Path getDirectory(){
            return getOuterReference().directory.resolve(getFormattedName());
        }

        private InstanceManifest getOuterReference(){
            return outerReference;
        }

        private void setOuterReference(InstanceManifest instance){
            outerReference = instance;
        }
    }
}
