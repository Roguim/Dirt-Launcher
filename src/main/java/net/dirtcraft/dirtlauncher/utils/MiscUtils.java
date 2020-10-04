package net.dirtcraft.dirtlauncher.utils;

import com.google.common.base.Strings;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.configuration.Constants;
import net.dirtcraft.dirtlauncher.logging.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiscUtils {

    public static boolean inIde() {
        Pattern pattern = Pattern.compile("^file:/");
        Matcher matcher = pattern.matcher(MiscUtils.class.getResource(MiscUtils.class.getSimpleName()+".class").toString());
        return matcher.find();
    }

    public static Image getImage(String... directory) {
        return new Image(getResourceStream(directory));
    }

    public static String getCssPath(String... directory){
        String path = "/" + String.join("/" , directory);
        try{
            return MiscUtils.class.getResource(path).toString();
        } catch (Exception e){
            path = path.replaceAll("\\.css$", ".bss");
            return MiscUtils.class.getResource(path).toString();
        }
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

    public static void updateLauncher() {
        final File currentJar = getCurrentJar();
        final File currentDir = currentJar.getParentFile();
        final File bootstrapper = new File(currentDir, getBootstrapJar());
        try(
                InputStream is = Main.class.getClassLoader().getResourceAsStream(getBootstrapJar());
                BufferedInputStream bis = new BufferedInputStream(Objects.requireNonNull(is));
        ){
            final boolean cleaned = bootstrapper.delete();
            Files.copy(bis, bootstrapper.toPath());
            List<String> args = Arrays.asList(getRuntime(), "-jar", bootstrapper.toString(), getRuntime(), currentJar.toString(), getUpdateUrl());
            args = new ArrayList<>(args);
            args.addAll(Main.getOptions());
            new ProcessBuilder(args.toArray(new String[0]))
                    .inheritIO()
                    .start();
            System.exit(0);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void trySleep(long ms){
        try{
            Thread.sleep(ms);
        } catch (Exception ignored){

        }
    }

    public static Optional<URL> getURL(String url){
        try {
            return Optional.of(new URL(url));
        } catch (MalformedURLException e){
            return Optional.empty();
        }
    }

    private static File getCurrentJar(){
        try {
            return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new Error(e);
        }
    }

    private static String getRuntime(){
        return Main.getConfig().getDefaultRuntime();
    }

    private static String getBootstrapJar(){
        return Constants.BOOTSTRAP_JAR;
    }

    private static String getUpdateUrl(){
        return Constants.UPDATE_URL;
    }

    public static TimerTask toTimerTask(Runnable runnable){
        return new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        };
    }

}
