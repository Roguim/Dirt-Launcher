package net.dirtcraft.dirtlauncher.configuration.manifests;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.configuration.ConfigBase;
import net.dirtcraft.dirtlauncher.logging.Logger;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;

import java.io.File;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public abstract class InstallationManifest<T> extends ConfigBase<T> {
    private final Supplier<T> tFactory;

    public InstallationManifest(File path, TypeToken<T> type, Supplier<T> tFactory){
        super(path, type);
        this.tFactory = tFactory;
    }

    @Override
    public void load(){
        try {
            configBase = JsonUtils.parseJson(path, type, this::migrate, t -> !t.toString().equals("{}")).orElse(null);
        } catch (Exception e){
            Logger.INSTANCE.error(e);
        }
        if (configBase == null) configBase = tFactory.get();
    }

    protected abstract T migrate(JsonObject jsonObject);

}
