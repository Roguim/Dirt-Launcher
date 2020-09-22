package net.dirtcraft.dirtlauncher.data.Curse;

import java.util.List;

public class CurseMinecraft {
    private CurseMinecraft(int i) throws InstantiationException {
        throw new InstantiationException("This is a data class intended to only be constructed by GSON.");
    }
    public final String version;
    public final List<CurseModLoader> modLoaders;

}
