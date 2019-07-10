package net.dirtcraft.dirtlauncher.backend.Utils;

import com.google.common.base.Strings;
import javafx.scene.image.Image;
import net.dirtcraft.dirtlauncher.Main;

import java.io.InputStream;
import java.net.URL;

public class Utility {
    public static Image getImage(String... directory){
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
