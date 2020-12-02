package net.dirtcraft.dirtlauncher.data.FTB;

public class ProjectVersion {
    private ProjectVersion(int i) throws InstantiationException {
        throw new InstantiationException("This is a data class intended to only be constructed by GSON.");
    }
    private final Specs specs;
    private final int id;
    private final String name;
    private final String type;
    private final long updated;
}
