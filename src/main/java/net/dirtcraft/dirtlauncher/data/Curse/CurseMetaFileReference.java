package net.dirtcraft.dirtlauncher.data.Curse;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.configuration.Constants;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.IDownload;
import net.dirtcraft.dirtlauncher.logging.Logger;
import net.dirtcraft.dirtlauncher.logging.VerboseLogger;
import net.dirtcraft.dirtlauncher.utils.WebUtils;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class CurseMetaFileReference implements IDownload {
    private CurseMetaFileReference(int i) throws InstantiationException {
        throw new InstantiationException("This is a data class intended to only be constructed by GSON.");
    }
    private transient @Nullable CurseFile manifest;
    public final long projectID;
    public final long fileID;
    public final boolean required;

    public String getDownloadUrl() {
        Logger logger = VerboseLogger.INSTANCE;
        try {
            URL requestURL = new URL(String.format("https://api.modpacks.ch/public/mod/%s", projectID));
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) requestURL.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Content-Type", "application/json");

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader output = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    java.lang.String responseLine;
                    StringBuffer response = new StringBuffer();

                    while ((responseLine = output.readLine()) != null) {
                        response.append(responseLine);
                    }
                    output.close();

                    Gson g = new Gson();
                    JsonObject responseData = g.fromJson(response.toString(), JsonObject.class);
                    JsonArray versions = responseData.get("versions").getAsJsonArray();
                    for (Iterator key = versions.iterator(); key.hasNext();) {
                        JsonObject version = (JsonObject) key.next();
                        if (version.get("id").getAsLong() == fileID) {
                            return version.get("url").toString().replaceAll("\"","");
                        }
                    }
                } else {
                    logger.error(responseCode);
                }
            } catch (IOException e) {
                logger.error(e);
            }
        } catch (MalformedURLException e) {
            logger.error(e);
        }
        return null;
    }

    public CompletableFuture<CurseFile> getManifestAsync(Executor executor){
        return CompletableFuture.supplyAsync(this::getManifest, executor);
    }

    @SuppressWarnings("UnstableApiUsage")
    private CurseFile getManifest(){
        if (this.manifest != null) return manifest;
        final TypeToken<CurseFile> type = new TypeToken<CurseFile>(){};
        return (manifest = WebUtils.getGsonFromUrl(getDownloadUrl(), type).orElse(null));
    }

    public boolean equals(CurseMetaFileReference o){
        return projectID == o.projectID && fileID == o.fileID && required == o.required;
    }

    public boolean isRequired(){
        return required;
    }

    @Override
    public long getSize() {
        return getManifest().getSize();
    }

    @Override
    public void setSize(long size) {
        getManifest().setSize(size);
    }

    @Override
    public URL getUrl() {
        return getManifest().getUrl();
    }

    @Override
    public String getFileName() {
        return getManifest().getFileName();
    }
}
