package net.dirtcraft.dirtlauncher.gui.home.sidebar;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.utils.Constants;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;

import java.io.IOException;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;

public class PackList extends ScrollPane {

    public PackList(){
        VBox packs = new VBox();
        updatePacksAsync(packs);
        packs.setFocusTraversable(false);
        packs.setAlignment(Pos.TOP_CENTER);

        ScrollPane sidebar = new ScrollPane();
        sidebar.setFitToWidth(true);
        sidebar.setFocusTraversable(false);
        sidebar.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sidebar.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sidebar.setPannable(true);
        MiscUtils.setAbsoluteWidth(sidebar, 300);
        AnchorPane.setTopAnchor(sidebar, 100d);
        AnchorPane.setLeftAnchor(sidebar, 0d);
        AnchorPane.setBottomAnchor(sidebar, 0d);
        sidebar.setContent(packs);
    }

    private CompletableFuture updatePacksAsync(VBox packList){
        return CompletableFuture.runAsync(()-> {
            ObservableList<Pack> packs = FXCollections.observableArrayList();
            JsonElement json = new JsonParser().parse(getStringFromURL());

            for (JsonElement element : json.getAsJsonArray()) {
                packs.add(new Pack(element.getAsJsonObject()));
            }

            packs.sort(Comparator.comparing(Pack::getName));
            if (Constants.VERBOSE && false) {
                new Thread(()->{
                    while (Main.getLogger() == null) {
                        try{
                            Thread.sleep(250); //wait for logger!
                        } catch (InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                    Main.getLogger().info(packs);
                }).start();
            }
            packList.getStyleClass().add(Constants.CSS_CLASS_VBOX);
            Platform.runLater(() -> {
                packList.getChildren().clear();
                packList.getChildren().addAll(packs);
            });
        });
    }

    private static String getStringFromURL() {
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
