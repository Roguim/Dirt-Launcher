package net.dirtcraft.dirtlauncher.game.installation.manifests;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.configuration.ConfigBase;
import net.dirtcraft.dirtlauncher.configuration.Manifests;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public abstract class InstallationManifest<T> extends ConfigBase<T> {
    private final Supplier<T> tFactory;

    public InstallationManifest(File path, TypeToken<T> type, Supplier<T> tFactory){
        super(path, type);
        this.tFactory = tFactory;
    }

    @Override
    public void load(){
        configBase = JsonUtils.parseJson(path, type, this::migrate, t-> !t.toString().equals("{}")).orElse(tFactory.get());
    }

    protected abstract T migrate(JsonObject jsonObject);

}
