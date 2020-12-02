package net.dirtcraft.dirtlauncher.data.FTB;

import java.net.URL;

public class Author {
    private Author(int i) throws InstantiationException {
        throw new InstantiationException("This is a data class intended to only be constructed by GSON");
    }
    private final URL website;
    private final int id;
    private final String name;
    private final String type;
    private final long updated;
}
