package net.dirtcraft.dirtlauncher.utils;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dirtcraft.dirtlauncher.Main;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

import static net.dirtcraft.dirtlauncher.configuration.Constants.MAX_DOWNLOAD_ATTEMPTS;

public class WebUtils {

    private static String jsonVersion = null;

    public static JsonObject getJsonFromUrl(String url) {
        try {
            return new JsonParser().parse(getJsonStringFromUrl(url)).getAsJsonObject();
        } catch (Exception exception) {
            System.out.println(exception.getMessage() + "\nRetrying...");
            return getJsonFromUrl(url);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <T> Optional<T> getGsonFromUrl(String url, TypeToken<T> type) {
        try {
            return Optional.ofNullable(Main.gson.fromJson(getJsonStringFromUrl(url), type.getType()));
        } catch (Exception exception) {
            exception.printStackTrace();
            return Optional.empty();
        }
    }

    public static String getJsonStringFromUrl(String url) {
        HttpResponse httpResponse = null;
        try {
            HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();
            HttpRequest httpRequest = requestFactory.buildGetRequest(new GenericUrl(url));
            httpResponse = httpRequest.execute();
            return httpResponse.parseAsString();
        } catch (Exception exception) {
            try {Thread.sleep(2000);} catch (InterruptedException ignored) {}
            System.out.println(exception.getMessage() + "\nRetrying...");
            return getJsonStringFromUrl(url);
        } finally {
            if (httpResponse != null) try{httpResponse.disconnect();}catch (IOException ignored){};
        }
    }

    @Nullable
    public static JsonObject getVersionManifestJson(String versionID) {
        JsonObject versionsList = getJsonFromUrl("https://launchermeta.mojang.com/mc/game/version_manifest.json");
        for(JsonElement version : versionsList.getAsJsonArray("versions")) {
            JsonObject versionJson = version.getAsJsonObject();
            if(versionJson.get("id").getAsString().equals(versionID)) return getJsonFromUrl(versionJson.get("url").getAsString());
        }
        return null;
    }

    public static String getLatestVersion() {
        if (jsonVersion != null) return jsonVersion;
        JsonObject versionJson = getJsonFromUrl("http://164.132.201.67/launcher/version.json");
        jsonVersion = versionJson.get("version").getAsString();
        return jsonVersion;
    }

    public static void copyURLToFile(String URL, File file) throws IOException{
        copyURLToFile(URL, file, 0);
    }

    private static void copyURLToFile(String URL, File file, int attempts) throws IOException {
        try {
            org.apache.commons.io.FileUtils.copyURLToFile(new URL(URL), file);
        } catch (IOException e){
            e.printStackTrace();
            if (attempts < MAX_DOWNLOAD_ATTEMPTS) copyURLToFile(URL, file, attempts+1);
            else throw e;
        }
    }
}
