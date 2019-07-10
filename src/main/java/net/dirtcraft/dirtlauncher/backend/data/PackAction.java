package net.dirtcraft.dirtlauncher.backend.data;

public enum  PackAction {
    INSTALL,
    UPDATE,
    PLAY;

    @Override
    public String toString() {
        if (this == INSTALL) return "Install";
        if (this == UPDATE) return "Update";
        if (this == PLAY) return "Play";
        return null;
    }
}
