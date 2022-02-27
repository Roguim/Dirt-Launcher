package net.dirtcraft.dirtlauncher.lib.data.json.mojang;

import java.util.Map;

public class GameAssetManifest {
    public GameAssetManifest(int i) throws InstantiationException{
        throw new InstantiationException("Gson data class. Not to be manually created.");
    }
    private final Map<String,Asset> objects;

    public Map<String, Asset> getObjects() {
        return objects;
    }
}
