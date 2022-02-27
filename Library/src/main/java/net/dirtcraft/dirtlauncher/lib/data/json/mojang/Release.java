package net.dirtcraft.dirtlauncher.lib.data.json.mojang;

import java.net.URL;

public class Release {
    public Release(int i) throws InstantiationException {
        throw new InstantiationException("Gson data class. Not to be manually created.");
    }
    private final String id;
    private final String type;
    private final URL url;
    private final String time;
    private final String releaseTime;

    public String getId() {
        return id;
    }

    public URL getUrl() {
        return url;
    }
}
