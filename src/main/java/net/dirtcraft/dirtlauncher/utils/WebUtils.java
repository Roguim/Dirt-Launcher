package net.dirtcraft.dirtlauncher.utils;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.annotation.Nullable;

public class WebUtils {

    public static JsonObject getJsonFromUrl(String url) {
        try {
            HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();
            HttpRequest httpRequest = requestFactory.buildGetRequest(new GenericUrl(url));
            HttpResponse httpResponse = httpRequest.execute();
            String response = httpResponse.parseAsString();
            httpResponse.disconnect();
            return new JsonParser().parse(response).getAsJsonObject();
        } catch (Exception exception) {
            System.out.println(exception.getMessage() + "\nRetrying...");
            try { Thread.sleep(2000); } catch (InterruptedException ignore) {}
            return getJsonFromUrl(url);
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
        JsonObject versionJson = getJsonFromUrl("http://164.132.201.67/launcher/version.json");
        return versionJson.get("version").getAsString();
    }
}
