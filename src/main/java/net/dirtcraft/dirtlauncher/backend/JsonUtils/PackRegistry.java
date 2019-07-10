package net.dirtcraft.dirtlauncher.backend.JsonUtils;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PackRegistry {

    private static final String JSON_URL = "http://164.132.201.67/launcher/packs.json";

    public static List<Pack> getPacks() {
        JsonElement json = new JsonParser().parse(getStringFromURL());

        List<Pack> packs = new ArrayList<>();
        for (JsonElement element : json.getAsJsonArray()) {
            packs.add(new Pack(element.getAsJsonObject()));
        }

        packs.sort(Comparator.comparing(Pack::getName));

        for (Pack pack : packs) {
            System.out.println("Name: " + pack.getName());
            System.out.println("Version: " + pack.getVersion());
            System.out.println("Code: " + pack.getCode());
            System.out.println("Pack Type: " + pack.getPackType());
            System.out.println("Link: " + pack.getLink());
            System.out.println("Splash: " + pack.getSplash());
            System.out.println("Logo: " + pack.getLogo());
            System.out.println("Game Version: " + pack.getGameVersion());
            System.out.println("Required Ram: " + pack.getRequiredRam());
            System.out.println("Recommended Ram: " + pack.getRecommendedRam());
            System.out.println("Forge Version: " + pack.getForgeVersion());
            for (OptionalMod optionalMod : pack.getOptionalMods()) {
                System.out.println("[Optional Mod] - Name: " + optionalMod.getName());
                System.out.println("[Optional Mod] - Version: " + optionalMod.getVersion());
                System.out.println("[Optional Mod] - Link: " + optionalMod.getLink());
                System.out.println("[Optional Mod] - Description: " + optionalMod.getDescription());
            }
        }

        return packs;

    }
    /*
    public static <T extends List> T getPacks(T genericList){
        if (packs == null) loadPacks();
        packs.forEach(pack->genericList.add(pack));
        return genericList;
    }
    */

    /*
    public static List<Pack> getPacks(){
        //if (packs == null) loadPacks();
        while (packs == null) loadPacks();
        return packs;
    }*/

    private static String getStringFromURL() {
        String string = null;
        try {
            HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();
            HttpRequest request = requestFactory.buildGetRequest(new GenericUrl(JSON_URL));
            HttpResponse response = request.execute();
            string = response.parseAsString();
            response.disconnect();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return string;
    }



}