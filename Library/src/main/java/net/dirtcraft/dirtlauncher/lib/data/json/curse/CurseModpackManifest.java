package net.dirtcraft.dirtlauncher.lib.data.json.curse;

import java.util.List;

public class CurseModpackManifest {
    private CurseModpackManifest(int i) throws InstantiationException {
        throw new InstantiationException("This is a data class intended to only be constructed by GSON.");
    }
    public final CurseMinecraft minecraft;
    public final String manifestType;
    public final String manifestVersion;
    public final String name;
    public final String version;
    public final String author;
    public final List<CurseMetaFileReference> files;

}
