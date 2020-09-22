package net.dirtcraft.dirtlauncher.data.FTB;

import java.util.List;

public class FTBModpackManifest {
    private FTBModpackManifest(int i) throws InstantiationException {
        throw new InstantiationException("This is a data class intended to only be constructed by GSON");
    }
    public final List<FTBFile> files;
    public final String name;
    public final List<FTBTarget> targets;
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
