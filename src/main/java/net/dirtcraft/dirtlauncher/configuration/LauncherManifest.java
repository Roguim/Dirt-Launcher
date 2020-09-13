package net.dirtcraft.dirtlauncher.configuration;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public abstract class LauncherManifest<T> extends ConfigBase<ArrayList<T>> {

    public LauncherManifest(File path, TypeToken<ArrayList<T>> type){
        super(path, type);
        load();
    }

    public ListIterator<T> listIterator(){
        return configBase.listIterator();
    }

    public Stream<T> stream(){
        return configBase.stream();
    }

    public void load(){
        configBase = FileUtils.parseJson(path, type, this::migrate).orElse(new ArrayList<>());
    }

    protected abstract ArrayList<T> migrate(JsonObject jsonObject);

}
