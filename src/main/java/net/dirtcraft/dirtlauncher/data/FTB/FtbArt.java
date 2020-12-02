package net.dirtcraft.dirtlauncher.data.FTB;

import java.net.URL;

public class FtbArt {
    private FtbArt(int i) throws InstantiationException {
        throw new InstantiationException("This is a data class intended to only be constructed by GSON");
    }
    private final int width;
    private final int height;
    private final boolean compressed;
    private final URL url;
    private final String sha1;
    private final long size;
    private final int id;
    private final String type;
    private final long updated;
}
