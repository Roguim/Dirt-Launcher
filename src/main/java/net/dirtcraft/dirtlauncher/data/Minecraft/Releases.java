package net.dirtcraft.dirtlauncher.data.Minecraft;

import java.util.List;
import java.util.Map;

@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class Releases {
    public Releases(int i) throws InstantiationException {
        throw new InstantiationException("Gson data class. Not to be manually created.");
    }
    private final Map<String,String> latest;
    private final List<Release> versions;

    public List<Release> getReleases() {
        return versions;
    }

    public String getLatestRelease() {
        return latest.get("release");
    }

    public String getLatestSnapshot() {
        return latest.get("snapshot");
    }
}
