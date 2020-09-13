package net.dirtcraft.dirtlauncher.configuration;

import com.google.common.reflect.TypeToken;
import net.dirtcraft.dirtlauncher.utils.FileUtils;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("UnstableApiUsage")
public abstract class ConfigBase<T> {
    private final Semaphore saveLock = new Semaphore(1);
    private final AtomicBoolean pendingSave = new AtomicBoolean(false);
    protected final File path;
    protected final TypeToken<T> type;
    protected T configBase;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public ConfigBase(File path, TypeToken<T> type) {
        path.getParentFile().mkdirs();
        this.path = path;
        this.type = type;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void load(){
        configBase = FileUtils.parseJson(path, type).get();
    }

    public void saveAsync(){
        if (!saveLock.tryAcquire()) pendingSave.set(true);
        else CompletableFuture.runAsync(this::save).whenComplete((v, e)-> saveLock.release());
    }

    private void save(){
        FileUtils.toJson(path, configBase, type);
        if (pendingSave.getAndSet(false)) save();
    }
}
