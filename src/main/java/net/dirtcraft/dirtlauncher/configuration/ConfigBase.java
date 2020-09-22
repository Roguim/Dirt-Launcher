package net.dirtcraft.dirtlauncher.configuration;

import com.google.common.reflect.TypeToken;
import net.dirtcraft.dirtlauncher.logging.Logger;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("UnstableApiUsage")
public abstract class ConfigBase<T> {
    protected final Semaphore saveLock = new Semaphore(1);
    protected final AtomicBoolean pendingSave = new AtomicBoolean(false);
    protected final File path;
    protected final TypeToken<T> type;
    protected T configBase;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public ConfigBase(File path, TypeToken<T> type) {
        path.getParentFile().mkdirs();
        this.path = path;
        this.type = type;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public ConfigBase(File path, Class<T> type) {
        path.getParentFile().mkdirs();
        this.path = path;
        this.type = TypeToken.of(type);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void load(){
        configBase = JsonUtils.parseJson(path, type).get();
    }

    public void saveAsync(){
        if (!saveLock.tryAcquire()) pendingSave.set(true);
        else CompletableFuture.runAsync(this::save).whenComplete((v,e)->saveLock.release());
    }

    private void save(){
        try {
            JsonUtils.toJson(path, configBase, type);
            if (pendingSave.getAndSet(false)) save();
        } catch (Exception e){
            Logger.INSTANCE.error(e);
        }
    }
}
