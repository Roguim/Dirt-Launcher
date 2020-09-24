package net.dirtcraft.dirtlauncher.utils;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dirtcraft.dirtlauncher.Main;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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
            try { Thread.sleep(2000); } catch (Exception ignored) {}
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

    public static String getStringFromUrl(String url) {
        HttpResponse httpResponse = null;
        try {
            HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();
            HttpRequest httpRequest = requestFactory.buildGetRequest(new GenericUrl(url));
            httpResponse = httpRequest.execute();
            return httpResponse.parseAsString();
        } catch (Exception exception) {
            try {Thread.sleep(2000);} catch (InterruptedException ignored) {}
            System.out.println(exception.getMessage() + "\nRetrying...");
            return getStringFromUrl(url);
        } finally {
            if (httpResponse != null) try{httpResponse.disconnect();}catch (IOException ignored){};
        }
    }

    public static String getJsonStringFromUrl(String url) {
        return getStringFromUrl(url);
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

    public static void copyURLToFileAsBrowser(URL url, File file) throws Exception{
        System.out.println("Downloading new update, Please wait...");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.72 Safari/537.36");
        FileUtils.copyInputStreamToFile(con.getInputStream(), file);
    }

    public static String executeGet(URL url) {
        String ret = "";
        try {

            HttpsURLConnection con;
            con = (HttpsURLConnection) url.openConnection();
            con.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.72 Safari/537.36");
            ret = con.getContent().toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }
}
