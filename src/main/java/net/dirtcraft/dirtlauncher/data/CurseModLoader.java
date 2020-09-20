package net.dirtcraft.dirtlauncher.data;

public class CurseModLoader {
    public final String id;
    public final boolean primary;

    private CurseModLoader(int i) throws InstantiationException {
        throw new InstantiationException("This is a data class intended to only be constructed by GSON.");
    }
}
