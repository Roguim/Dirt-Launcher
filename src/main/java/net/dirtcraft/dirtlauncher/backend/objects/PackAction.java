package net.dirtcraft.dirtlauncher.backend.objects;

public enum PackAction {
    INSTALL,
    UPDATE,
    PLAY;

    @Override
    public String toString() {
        switch (this) {
            case PLAY:
                return "Play";
            case UPDATE:
                return "Update";
            default:
            case INSTALL:
                return "Install";
        }
    }
}
