package net.dirtcraft.dirtlauncher.providers;

import com.google.common.reflect.TypeToken;
import net.dirtcraft.dirtlauncher.configuration.Constants;
import net.dirtcraft.dirtlauncher.data.CurseProject;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.utils.WebUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;

public class CurseProvider implements IPackProvider {
    @Override
    public boolean isOutdated(Modpack modpack) {
        return false;
    }

    private Optional<CurseProject> getInstanceFromUrl(URL url) {
        final String name = url.getPath().replaceAll("/minecraft/modpacks/(.*)/?", "$1");
        final String pattern = "(?i)^" + name.replaceAll("-", "[-. ]") + "$";
        final Optional<ArrayList<CurseProject>> optListing = getProjectsMatching(name);
        return optListing.flatMap(curseProjects -> curseProjects.stream()
                .filter(i -> i.name.matches(pattern))
                .findFirst());
    }

    @SuppressWarnings("UnstableApiUsage")
    private Optional<ArrayList<CurseProject>> getProjectsMatching(String name){
        try {
            final String query = Constants.CURSE_API_URL + "search?categoryId=0&gameId=432&gameVersion=&index=0&pageSize=2500&searchFilter=" + name + "&sectionId=4471&sort=0";
            return WebUtils.getGsonFromUrl(query, new TypeToken<ArrayList<CurseProject>>(){});
        } catch (Exception e){
            return Optional.empty();
        }
    }

}
