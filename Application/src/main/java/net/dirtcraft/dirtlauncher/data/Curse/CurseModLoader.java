package net.dirtcraft.dirtlauncher.data.Curse;

public class CurseModLoader {
    private CurseModLoader(int i) throws InstantiationException {
        throw new InstantiationException("This is a data class intended to only be constructed by GSON.");
    }
    public final String id;
    public final boolean primary;
}
