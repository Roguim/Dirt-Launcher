package net.dirtcraft.dirtlauncher.configuration.manifests;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.configuration.LauncherManifest;

import java.util.ArrayList;

@SuppressWarnings("UnstableApiUsage")
public class InstanceManifest extends LauncherManifest<InstanceManifest.Entry> {

    public InstanceManifest(){
        super(Main.getConfig().getDirectoryManifest(Main.getConfig().getInstancesDirectory()), new TypeToken<ArrayList<Entry>>(){});
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
            packs.add(new Entry(name, version, gameVersion, forgeVersion));
        }
        return packs;
    }

    public static class Entry{
        public final String name;
        public final String version;
        public final String gameVersion;
        public final String forgeVersion;

        public Entry(String name, String version, String gameVersion, String forgeVersion){
            this.name = name;
            this.version = version;
            this.gameVersion = gameVersion;
            this.forgeVersion = forgeVersion;
        }
    }
}
