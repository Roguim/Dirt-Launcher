package net.dirtcraft.dirtlauncher.data.FTB;

import java.util.List;

public class ModpackManifest {
    private ModpackManifest(int i) throws InstantiationException {
        throw new InstantiationException("This is a data class intended to only be constructed by GSON");
    }
    public final List<File> files;
    public final String name;
    public final List<Target> targets;
    public final int installs;
    public final int plays;
    public final int refreshed;
    public final String changelog;
    public final int parent;
    public final String notification;
    public final List<String> links;
    public final String status;
    public final int id;
    public final String type;
    public final String updated;
}
