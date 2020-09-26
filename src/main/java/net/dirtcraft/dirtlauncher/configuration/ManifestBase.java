package net.dirtcraft.dirtlauncher.configuration;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.logging.Logger;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;

import java.nio.file.Path;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public abstract class ManifestBase<T> extends ConfigBase<T> {
    private final Supplier<T> tFactory;
    public ManifestBase(Path path, TypeToken<T> type, Supplier<T> tFactory){
        super(path.resolve("manifest.json").toFile(), type);
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
