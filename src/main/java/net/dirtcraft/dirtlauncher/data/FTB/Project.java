package net.dirtcraft.dirtlauncher.data.FTB;

import java.util.List;

public class Project {
    private Project(int i) throws InstantiationException {
        throw new InstantiationException("This is a data class intended to only be constructed by GSON");
    }
    private final String synopsis;
    private final String description;
    private final FtbArt art;
    //private final Object[] links;
    private final List<Author> authors;
    private final List<ProjectVersion> versions;
    //private final int installs;
    //private final int plays;
    //private final boolean featured;
    //private final long refreshed;
    //private final String notification;
    //private final Object rating;
    private final String status;
    private final int id;
    private final String name;
    private final String type;
    private final long updated;
    //private final Object[] tags;



}
