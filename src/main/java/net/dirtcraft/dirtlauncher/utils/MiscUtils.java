package net.dirtcraft.dirtlauncher.utils;

import com.google.common.base.Strings;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

import java.awt.*;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiscUtils {

    public static boolean inIde() {
        Pattern pattern = Pattern.compile("^file:/");
        Matcher matcher = pattern.matcher(MiscUtils.class.getResource(MiscUtils.class.getSimpleName()+".class").toString());
        return matcher.find();
    }

    public static final Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();

    public static Image getImage(String... directory) {
        return new Image(getResourceStream(directory));
    }

    public static String getResourcePath(String... directory){
        return getResourceURL(directory).toString();
    }

    public static InputStream getResourceStream(String... directory){
        String path = "/" + String.join("/", directory);
        return MiscUtils.class.getResourceAsStream(path);
    }

    public static URL getResourceURL(String... directory){
        String path = "/" + String.join("/" , directory);
        return MiscUtils.class.getResource(path);
    }

    public static boolean isEmptyOrNull(String... strings) {
        for (String string : strings) {
            if (Strings.isNullOrEmpty(string)) return true;
        }
        return false;
    }

    public static ImageView getGraphic(int size, String... dir) {
        return getGraphic(size, size, dir);
    }

    public static ImageView getGraphic(int width, int height, String... dir) {
        ImageView graphic = new ImageView();
        graphic.setFitHeight(height);
        graphic.setFitWidth(width);
        graphic.setImage(MiscUtils.getImage(dir));
        return graphic;
    }

}
