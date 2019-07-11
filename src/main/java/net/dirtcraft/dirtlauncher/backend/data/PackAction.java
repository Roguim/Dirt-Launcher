package net.dirtcraft.dirtlauncher.backend.data;

public enum  PackAction {
    INSTALL,
    UPDATE,
    PLAY;

    @Override
    public String toString() {
        switch (this){
            case PLAY: return "Play";
            case UPDATE: return "Update";
            case INSTALL:
            default: return "Install";
        }
    }
}
