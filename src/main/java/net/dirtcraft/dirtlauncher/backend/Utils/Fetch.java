package net.dirtcraft.dirtlauncher.backend.Utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import net.dirtcraft.dirtlauncher.backend.data.PackRegistry;
import org.apache.commons.text.WordUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Fetch {

    public ObservableList<HBox> getPacks() {
        ObservableList<HBox> packs = FXCollections.observableArrayList();

        final JsonObject json = getJsonObjFromString(getStringFromURL(getFinalURL(PackRegistry.JSON_URL)));
        json.get("Pack Names").getAsJsonArray().forEach(element -> {
            String packName = WordUtils.capitalizeFully(element.getAsString());
            Text label = new Text(packName);
            label.setFont(Font.font("System Bold", FontWeight.LIGHT, 20));
            label.setTextAlignment(TextAlignment.CENTER);
            label.setFill(Paint.valueOf("WHITE"));

            HBox hBox = new HBox();
            hBox.setStyle("-fx-background-color: firebrick; -fx-padding: 0px;");
            hBox.setPrefHeight(30);
            hBox.setAlignment(Pos.CENTER);
            hBox.getChildren().add(label);
            hBox.setOnMouseClicked((t)->System.out.println(packName));


            packs.add(hBox);
        });
        /*for (Pack pack : packRegistry.getPackList().getPacks()) {
            packs.add(pack.getName());
        }*/
        return packs;
    }

    private String getFinalURL(String URL) {
        try {
            URL url = new URL(URL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("HEAD");
            con.setInstanceFollowRedirects(false);
            con.addRequestProperty("User-Agent", "Mozilla");
            con.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            con.addRequestProperty("Referer", "https://www.google.com/");
            con.connect();
            int resCode = con.getResponseCode();
            if (resCode == HttpURLConnection.HTTP_SEE_OTHER
                    || resCode == HttpURLConnection.HTTP_MOVED_PERM
                    || resCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                String Location = con.getHeaderField("Location");
                if (Location.startsWith("/")) {
                    Location = url.getProtocol() + "://" + url.getHost() + Location;
                }
                return getFinalURL(Location);
            }
            con.disconnect();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return URL;
    }

    private String getStringFromURL(String url) {
        try (InputStream in = new URL(url).openStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            return out.toString();
        } catch (IOException e) {
            return e.toString();
        }
    }

    private JsonObject getJsonObjFromString(String jsonString) {
        return new JsonParser().parse(jsonString).getAsJsonObject();
    }

}
