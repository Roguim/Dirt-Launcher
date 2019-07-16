package net.dirtcraft.dirtlauncher.backend.utils;

import com.google.common.base.Strings;
import javafx.scene.image.Image;
import net.dirtcraft.dirtlauncher.Main;

import java.awt.*;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiscUtils {

    public static boolean inIde() {
        Class main = Main.getInstance().getClass();
        Pattern pattern = Pattern.compile("^file:/");
        Matcher matcher = pattern.matcher(main.getResource(main.getSimpleName()+".class").toString());
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

}
