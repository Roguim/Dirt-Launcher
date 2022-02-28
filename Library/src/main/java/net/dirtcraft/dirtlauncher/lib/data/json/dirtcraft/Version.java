package net.dirtcraft.dirtlauncher.lib.data.json.dirtcraft;

public class Version {
    public Version(int i) throws InstantiationException {
        throw new InstantiationException("Gson data class. Not to be manually created.");
    }
    public final String version;

    public String getVersion() {
        return version;
    }
}
