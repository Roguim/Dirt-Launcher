package net.dirtcraft.dirtlauncher.configuration;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;

import java.nio.file.Path;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public abstract class ManifestBase<T> extends ConfigBase<T> {
    public ManifestBase(Path path, TypeToken<T> type, Supplier<T> tFactory){
        super(path.resolve("manifest.json").toFile(), type, tFactory);
    }
    public ManifestBase(Path path, Class<T> type, Supplier<T> tFactory){
        super(path.resolve("manifest.json").toFile(), type, tFactory);
    }

    @Override
    public void load(){
        configBase = JsonUtils.parseJson(configFile, type, this::migrate, t -> !t.toString().equals("{}")).orElse(tFactory.get());
    }

    protected abstract T migrate(JsonObject jsonObject);

}
