package net.dirtcraft.dirtlauncher.utils;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dirtcraft.dirtlauncher.Main;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.SocketTimeoutException;

public class WebUtils {

    private static String jsonVersion = null;

    public static JsonObject getJsonFromUrl(String url) throws IOException {
        try {
            HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();
            HttpRequest httpRequest = requestFactory.buildGetRequest(new GenericUrl(url));
            HttpResponse httpResponse = httpRequest.execute();
            String response = httpResponse.parseAsString();
            httpResponse.disconnect();
            return new JsonParser().parse(response).getAsJsonObject();
        } catch (SocketTimeoutException | HttpResponseException exception) {
            Main.getLogger().warn(exception.getMessage() + "\nRetrying...");
            return getJsonFromUrl(url);
        }
    }

    @Nullable
    public static JsonObject getVersionManifestJson(String versionID) throws IOException {
        JsonObject versionsList = getJsonFromUrl("https://launchermeta.mojang.com/mc/game/version_manifest.json");
        for(JsonElement version : versionsList.getAsJsonArray("versions")) {
            JsonObject versionJson = version.getAsJsonObject();
            if(versionJson.get("id").getAsString().equals(versionID)) return getJsonFromUrl(versionJson.get("url").getAsString());
        }
        return null;
    }

    public static String getLatestVersion() throws IOException {
        if (jsonVersion != null) return jsonVersion;
        JsonObject versionJson = getJsonFromUrl("http://164.132.201.67/launcher/version.json");
        jsonVersion = versionJson.get("version").getAsString();
        return jsonVersion;
    }
}
