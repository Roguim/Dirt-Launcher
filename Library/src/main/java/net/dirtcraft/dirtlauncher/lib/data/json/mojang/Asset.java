package net.dirtcraft.dirtlauncher.lib.data.json.mojang;

public class Asset {
    public Asset(int i) throws InstantiationException {
        throw new InstantiationException("Gson data class. Not to be manually created.");
    }

    private final String hash;
    private final long size;

    public String getHash() {
        return hash;
    }

    public long getSize() {
        return size;
    }
}
