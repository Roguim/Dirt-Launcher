package net.dirtcraft.dirtlauncher.providers;

import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;

import java.net.URL;
import java.util.HashMap;
import java.util.Optional;

public interface IPackProvider {
    boolean isOutdated(Modpack modpack);
    //Optional<? extends Instance> getFromUrl(URL url);

    interface Instance{
        String getGameVersion();
        Modpack get();
    }

    class InstanceManager{

        private static HashMap<Modpack.UpdateTracker, IPackProvider> providers;

        static {
            init();
        }

        @SuppressWarnings("unchecked")
        public static <T> Optional<T> getInstance(Class<T> clazz){
            return (Optional<T>) providers.values().stream().filter(clazz::isInstance).findFirst();
        }

        /*
        public static Optional<IPackProvider> getInstance(Modpack.UpdateTracker tracker){
            return Optional.of(InstanceManager.providers.getOrDefault(tracker, null));
        }
         */

        public static void reload(){
            init();
        }

        private static void init(){
            providers = new HashMap<>();
            providers.put(Modpack.UpdateTracker.CURSE, new CurseProvider());
        }
    }
}
