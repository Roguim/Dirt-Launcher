package net.dirtcraft.dirtlauncher.configuration;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;

import java.nio.file.Path;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public abstract class ManifestBase<T> extends ConfigBase<T> {
    protected Path directory;
    public ManifestBase(Path directory, TypeToken<T> type, Supplier<T> tFactory){
        super(directory.resolve("manifest.json").toFile(), type, tFactory);
        this.directory = directory;
    }

    public Path getDirectory() {
        return directory;
    }

    @Override
    public void load(){
        configBase = JsonUtils.parseJson(configFile, type, this::migrate, t -> !t.toString().equals("{}")).orElse(tFactory.get());
    }

    protected abstract T migrate(JsonObject jsonObject);

}
