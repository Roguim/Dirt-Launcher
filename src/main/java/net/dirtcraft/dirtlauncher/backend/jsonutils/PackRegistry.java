package net.dirtcraft.dirtlauncher.backend.jsonutils;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.backend.utils.Constants;
import net.dirtcraft.dirtlauncher.backend.objects.OptionalMod;
import net.dirtcraft.dirtlauncher.elements.Pack;
import org.apache.logging.log4j.Logger;

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
        if (Constants.VERBOSE) {
            //Logger might take a little to appear, since we are loading it parallel to
            //this in another thread so we just wait for it to show our debug output...
            new Thread(()->{
                while (Main.getLogger() == null) {
                    try{
                        Thread.sleep(20);
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
                final Logger logger = Main.getLogger();
                for (Pack pack : packs) {
                    logger.info("Name: " + pack.getName());
                    logger.info("Version: " + pack.getVersion());
                    logger.info("Code: " + pack.getCode());
                    logger.info("Pack Type: " + pack.getPackType());
                    logger.info("Link: " + pack.getLink());
                    logger.info("Splash: " + pack.getSplash());
                    logger.info("Logo: " + pack.getLogo());
                    logger.info("Game Version: " + pack.getGameVersion());
                    logger.info("Required Ram: " + pack.getRequiredRam());
                    logger.info("Recommended Ram: " + pack.getRecommendedRam());
                    logger.info("Forge Version: " + pack.getForgeVersion());
                    for (OptionalMod optionalMod : pack.getOptionalMods()) {
                        logger.info("[Optional Mod] - Name: " + optionalMod.getName());
                        logger.info("[Optional Mod] - Version: " + optionalMod.getVersion());
                        logger.info("[Optional Mod] - Link: " + optionalMod.getLink());
                        logger.info("[Optional Mod] - Description: " + optionalMod.getDescription());
                    }
                }}).start();
        }
        return packs;
    }

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