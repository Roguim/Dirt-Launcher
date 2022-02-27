package net.dirtcraft.dirtlauncher.data.DirtCraft;

public class Version {
    public Version(int i) throws InstantiationException {
        throw new InstantiationException("Gson data class. Not to be manually created.");
    }
    private final String version;

    public String getVersion() {
        return version;
    }
}
