package net.dirtcraft.dirtlauncher.game.modpacks;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.dirtcraft.dirtlauncher.utils.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModpackManager {
    private static ModpackManager instance = new ModpackManager();
    private List<Modpack> modpacks;

    public ModpackManager(){
            List<Modpack> packsList = new ArrayList<>();
            JsonElement json = new JsonParser().parse(getStringFromURL());

            for (JsonElement element : json.getAsJsonArray()) {
                packsList.add(new Modpack(element.getAsJsonObject()));
            }

            modpacks = packsList;
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
}
