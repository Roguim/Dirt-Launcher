package net.dirtcraft.dirtlauncher.lib.data.json.ftb;

public class FTBTarget {
    private FTBTarget(int i) throws InstantiationException {
        throw new InstantiationException("This is a data class intended to only be constructed by GSON.");
    }
    public String version;
    public int id;
    public String name;
    public String type;
    public int updated;
}
