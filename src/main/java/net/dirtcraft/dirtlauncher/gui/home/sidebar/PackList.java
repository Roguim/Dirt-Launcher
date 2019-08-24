package net.dirtcraft.dirtlauncher.gui.home.sidebar;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import net.dirtcraft.dirtlauncher.utils.Constants;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class PackList extends ScrollPane {
    private Future<List<Pack>> listPreload;
    private final VBox packs;
    public PackList(){
        packs = new VBox();
        packs.getStyleClass().add(Constants.CSS_CLASS_VBOX);
        packs.setFocusTraversable(false);
        packs.setAlignment(Pos.TOP_CENTER);


        listPreload = CompletableFuture.supplyAsync(()-> {
            List<Pack> packsList = new ArrayList<>();
            JsonElement json = new JsonParser().parse(getStringFromURL());

            for (JsonElement element : json.getAsJsonArray()) {
                packsList.add(new Pack(element.getAsJsonObject()));
            }

            packsList.sort(Comparator.comparing(Pack::getName));
            return packsList;
        });

        setFitToWidth(true);
        setFocusTraversable(false);
        setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setPannable(true);
        MiscUtils.setAbsoluteWidth(this, 300);
        AnchorPane.setTopAnchor(this, 100d);
        AnchorPane.setLeftAnchor(this, 0d);
        AnchorPane.setBottomAnchor(this, 0d);
        setContent(packs);
    }

    public void updatePacksAsync(){
        CompletableFuture.runAsync(()-> {
            if (listPreload != null) {
                Platform.runLater(()->{
                    try {
                        packs.getChildren().clear();
                        packs.getChildren().addAll(listPreload.get());
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    listPreload = null;
                });
                return;
            }
            List<Pack> packsList = new ArrayList<>();
            JsonElement json = new JsonParser().parse(getStringFromURL());

            for (JsonElement element : json.getAsJsonArray()) {
                packsList.add(new Pack(element.getAsJsonObject()));
            }

            packsList.sort(Comparator.comparing(Pack::getName));
            Platform.runLater(() -> {
                packs.getChildren().clear();
                packs.getChildren().addAll(packsList);
            });
        });
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
}
