package net.dirtcraft.dirtlauncher.configuration;

import com.google.common.reflect.TypeToken;
import net.dirtcraft.dirtlauncher.logging.Logger;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public abstract class ConfigBase<T> {
    protected final Semaphore saveLock = new Semaphore(1);
    protected final AtomicBoolean pendingSave = new AtomicBoolean(false);
    protected final File configFile;
    protected final TypeToken<T> type;
    protected final Supplier<T> tFactory;
    protected T configBase;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public ConfigBase(File path, TypeToken<T> type, Supplier<T> tFactory) {
        path.getParentFile().mkdirs();
        this.tFactory = tFactory;
        this.configFile = path;
        this.type = type;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public ConfigBase(File path, Class<T> type, Supplier<T> tFactory) {
        path.getParentFile().mkdirs();
        this.tFactory = tFactory;
        this.configFile = path;
        this.type = TypeToken.of(type);
    }

    public void load(){
        configBase = JsonUtils.parseJson(configFile, type).orElse(tFactory.get());
    }

    public void saveAsync(){
        if (!saveLock.tryAcquire()) pendingSave.set(true);
        else CompletableFuture.runAsync(this::save).whenComplete((v,e)->saveLock.release());
    }

    private void save(){
        try {
            JsonUtils.toJson(configFile, configBase, type);
            if (pendingSave.getAndSet(false)) save();
        } catch (Exception e){
            Logger.INSTANCE.error(e);
        }
    }
}
