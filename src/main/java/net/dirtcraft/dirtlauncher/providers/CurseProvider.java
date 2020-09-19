package net.dirtcraft.dirtlauncher.providers;

import com.google.common.reflect.TypeToken;
import net.dirtcraft.dirtlauncher.configuration.Constants;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.utils.WebUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CurseProvider implements IPackProvider {
    @Override
    public boolean isOutdated(Modpack modpack) {
        return false;
    }

    @Override
    public Optional<? extends Instance> getFromUrl(URL url) {
        final String name = url.getPath().replaceAll("/minecraft/modpacks/(.*)/?", "$1");
        final String pattern = "(?i)^" + name.replaceAll("-", "[-. ]") + "$";
        final Optional<ArrayList<CurseInstance>> optListing = oof(name);
        if (!optListing.isPresent()) return Optional.empty();

        final List<CurseInstance> listing = optListing.get();
        return listing.stream().filter(i->i.name.matches(pattern)).findFirst();
    }

    @SuppressWarnings("UnstableApiUsage")
    private Optional<ArrayList<CurseInstance>> oof(String name){
        try {
            final String query = Constants.CURSE_API_URL + "search?categoryId=0&gameId=432&gameVersion=&index=0&pageSize=2500&searchFilter=" + name + "&sectionId=4471&sort=0";
            return WebUtils.getGsonFromUrl(query, new TypeToken<ArrayList<CurseInstance>>(){});
        } catch (Exception e){
            return Optional.empty();
        }
    }

    public static class CurseInstance implements Instance{
        public final long id;
        public final String name;


        @Override
        public String getGameVersion() {
            return null;
        }

        @Override
        public Modpack get() {
            return null;
        }

        //pseudo-constructor. Not to be used, like ever.
        private CurseInstance(int i){
            throw new Error();
        }
    }
}
