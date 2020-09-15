package net.dirtcraft.dirtlauncher.game.modpacks;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import javafx.application.Platform;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.configuration.Constants;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class ModpackManager {
    private static ModpackManager instance = new ModpackManager();
    private final File jsonPath;
    private final Gson gson;
    private final Semaphore saveLock = new Semaphore(1);
    private final AtomicBoolean pendingSave = new AtomicBoolean(false);
    private List<Modpack> modpacks;

    public ModpackManager() {
        jsonPath = Main.getLauncherDirectory().resolve("packs.json").toFile();
        gson = Main.gson;
        if (!jsonPath.exists()) {
            try {
                List<Modpack> packsList = new ArrayList<>();
                JsonElement json = new JsonParser().parse(getStringFromURL());

                for (JsonElement element : json.getAsJsonArray()) {
                    packsList.add(new Modpack(element.getAsJsonObject()));
                }
                modpacks = packsList;
                //noinspection ResultOfMethodCallIgnored
                jsonPath.createNewFile();
                saveAsync();
            } catch (IOException ignored) {
            }
        } else {
            load();
            updateToLatestAsync();
        }

        saveAsync();
    }

    private String getStringFromURL() {
        String string = null;
        try {
            HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();
            HttpRequest request = requestFactory.buildGetRequest(new GenericUrl(Constants.PACK_JSON_URL));
            HttpResponse response = request.execute();
            string = response.parseAsString();
            response.disconnect();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return string;
    }

    public List<Modpack> getModpacks(){
        return modpacks;
    }

    public static ModpackManager getInstance(){
        return instance;
    }

    public void saveAsync(){
        if (!saveLock.tryAcquire()) pendingSave.set(true);
        else CompletableFuture.runAsync(this::save).whenComplete((v, e)-> saveLock.release());
    }

    @SuppressWarnings("UnstableApiUsage")
    private void save(){
        try(
                FileWriter fw = new FileWriter(jsonPath);
                JsonWriter jw = new JsonWriter(fw);
        ){
            jw.setIndent("  ");
            gson.toJson(modpacks, new TypeToken<ArrayList<Modpack>>(){}.getType(), jw);
        }catch (IOException ignored){
        }
        if (pendingSave.getAndSet(false)) saveAsync();
    }

    @SuppressWarnings("UnstableApiUsage")
    public void load(){
        try(
                FileReader fr = new FileReader(jsonPath);
                JsonReader jr = new JsonReader(fr);
                ){
            modpacks = gson.fromJson(jr, new TypeToken<ArrayList<Modpack>>(){}.getType());
            if (modpacks == null) modpacks = new ArrayList<>();
        } catch (IOException ignored){
        }
    }

    public void updateToLatestAsync() {
        CompletableFuture.runAsync(this::updateToLatest);
    }

    private void updateToLatest(){
        ArrayList<Modpack> modpacks = new ArrayList<>(this.modpacks);
        JsonElement json;
        try {
            json = new JsonParser().parse(getStringFromURL());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        List<Modpack> remoteList = new ArrayList<>();

        for (JsonElement element : json.getAsJsonArray()) {
            remoteList.add(new Modpack(element.getAsJsonObject()));
        }

        for (Modpack remote : remoteList) {
            ListIterator<Modpack> iterator = modpacks.listIterator();
            boolean replaced = false;
            while (iterator.hasNext() && !replaced) {
                Modpack local = iterator.next();
                if (local.getName().equals(remote.getName())) {
                    remote.favourite = local.isFavourite();
                    iterator.set(remote);
                    replaced = true;
                }
            }
            if (!replaced) modpacks.add(remote);
        }
        this.modpacks = modpacks;
        Platform.runLater(Main.getHome()::updateModpacks);
    }
}
