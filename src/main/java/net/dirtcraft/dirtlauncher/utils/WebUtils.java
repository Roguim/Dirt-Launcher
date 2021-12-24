package net.dirtcraft.dirtlauncher.utils;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.data.DirtCraft.Version;
import net.dirtcraft.dirtlauncher.data.Minecraft.GameVersion;
import net.dirtcraft.dirtlauncher.data.Minecraft.Releases;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;

import static net.dirtcraft.dirtlauncher.configuration.Constants.MAX_DOWNLOAD_ATTEMPTS;

public class WebUtils {

    private static OkHttpClient client = new OkHttpClient();
    private static String jsonVersion = null;

    //todo fix this shit lol i dont even care anymore fucking have not coded in yonks and this is the shit i gotta deal with smh
    public static Response getResponse(Request request){
        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <T> Optional<T> getGsonFromRequest(Request request, TypeToken<T> type) {
        try {
            return Optional.ofNullable(Main.gson.fromJson(getResponse(request).body().string(), type.getType()));
        } catch (Exception exception) {
            exception.printStackTrace();
            return Optional.empty();
        }
    }

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

    public static <T> Optional<T> getGsonFromUrl(String url, Class<T> type) {
        try {
            return Optional.ofNullable(Main.gson.fromJson(getJsonStringFromUrl(url), type));
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

    public static Optional<GameVersion> getVersionManifestJson(String versionID) {
        return getGsonFromUrl("https://launchermeta.mojang.com/mc/game/version_manifest.json", Releases.class)
                .map(Releases::getReleases)
                .orElseGet(ArrayList::new)
                .stream()
                .filter(x->x.getId().matches(versionID))
                .findFirst()
                .flatMap(release->getGsonFromUrl(release.getUrl().toString(), GameVersion.class));
    }

    public static String getLatestVersion() throws IOException {
        if (jsonVersion == null) jsonVersion = getGsonFromUrl("http://164.132.201.67/launcher/version.json", Version.class)
                .map(Version::getVersion)
                .orElseThrow(()->new IOException("Version not found!"));
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
