package net.dirtcraft.dirtlauncher.backend.jsonutils;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

public class JsonFetcher {

    public static JsonObject getJsonFromUrl(String url) throws IOException {
        HttpRequestFactory requestFacotry = new NetHttpTransport().createRequestFactory();
        HttpRequest httpRequest = requestFacotry.buildGetRequest(new GenericUrl(url));
        HttpResponse httpResponse = httpRequest.execute();
        String response = httpResponse.parseAsString();
        httpResponse.disconnect();
        return new JsonParser().parse(response).getAsJsonObject();
    }

    public static JsonObject getVersionManifestJson(String versionID) throws IOException {
        JsonObject versionsList = getJsonFromUrl("https://launchermeta.mojang.com/mc/game/version_manifest.json");
        for(JsonElement version : versionsList.getAsJsonArray("versions")) {
            JsonObject versionJson = version.getAsJsonObject();
            if(versionJson.get("id").getAsString().equals(versionID)) return getJsonFromUrl(versionJson.get("url").getAsString());
        }
        return null;
    }
}
