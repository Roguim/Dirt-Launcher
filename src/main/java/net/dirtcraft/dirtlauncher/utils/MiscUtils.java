package net.dirtcraft.dirtlauncher.utils;

import com.google.common.base.Strings;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import net.dirtcraft.dirtlauncher.Main;

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
        Main main = Main.getInstance();
        String path = "/" + String.join("/", directory);
        return main.getClass().getResourceAsStream(path);
    }

    public static URL getResourceURL(String... directory){
        Main main = Main.getInstance();
        String path = "/" + String.join("/" , directory);
        return main.getClass().getResource(path);
    }

    public static boolean isEmptyOrNull(String... strings) {
        for (String string : strings) {
            if (Strings.isNullOrEmpty(string)) return true;
        }
        return false;
    }

    public static void setAbsoluteSize(Region node, double width, double height){
        node.setMaxSize(width,  height);
        node.setMinSize(width,  height);
    }

    public static void setAbsoluteWidth(Region node, double width){
        node.setMaxWidth(width);
        node.setMinWidth(width);
    }

    public static void setAbsoluteHeight(Region node, double height){
        node.setMaxHeight(height);
        node.setMinHeight(height);
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
