package net.dirtcraft.dirtlauncher.utils;

import com.google.common.base.Strings;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.configuration.Constants;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.DownloadManager;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.DownloadMeta;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.progress.Trackers;
import net.dirtcraft.dirtlauncher.gui.home.sidebar.PackSelector;
import net.dirtcraft.dirtlauncher.gui.wizards.Install;
import net.dirtcraft.dirtlauncher.logging.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void updateLauncher() {
        Platform.runLater(()->launchInstallScene("Fetching Launcher Update... Please wait"));
        ProgressContainer container = new ProgressContainer();
        DownloadManager manager = new DownloadManager();
        final File currentJar = getCurrentJar();
        final File currentDir = currentJar.getParentFile();
        final File bootstrapper = new File(currentDir, getBootstrapJar());
        final File temp = new File(getCurrentJar().toString() + ".tmp");
        try(
                InputStream is = Main.class.getClassLoader().getResourceAsStream(getBootstrapJar());
                BufferedInputStream bis = new BufferedInputStream(Objects.requireNonNull(is));
        ){
            bootstrapper.delete();
            DownloadMeta meta = new DownloadMeta(new URL(Constants.UPDATE_URL), temp);
            manager.download(Trackers.getTracker(container, "Fetching Launcher Update", "Downloading Launcher Update"), meta);
            Files.copy(bis, bootstrapper.toPath());
            List<String> args = Arrays.asList(getRuntime(), "-jar", bootstrapper.toString(), getRuntime(), currentJar.toString(), temp.toString());
            args = new ArrayList<>(args);
            args.addAll(Main.getOptions());
            new ProcessBuilder(args.toArray(new String[0]))
                    .inheritIO()
                    .start();
            System.out.println("\n\n");
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

    public static void launchInstallScene(PackSelector modPack) {
        launchInstallScene("Installing " + modPack.getModpack().getName() + "...");
    }

    public static void launchInstallScene(String modPack) {
        try {
            Stage stage = new Stage();
            stage.setTitle(modPack);
            Parent root = FXMLLoader.load(MiscUtils.getResourceURL(Constants.JAR_SCENES, "install.fxml"));

            stage.initOwner(Main.getHome().getStage());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initStyle(StageStyle.DECORATED);

            stage.getIcons().setAll(MiscUtils.getImage(Constants.JAR_ICONS, "install.png"));

            stage.setScene(new Scene(root, Main.screenDimension.getWidth() / 3, Main.screenDimension.getHeight() / 4));
            stage.setResizable(false);
            stage.setOnCloseRequest(Event::consume);

            stage.show();

            Install.getInstance().ifPresent(install -> {
                TextFlow notificationArea = install.getNotificationText();
                Text notification = new Text("Beginning Download...");
                notification.setFill(Color.WHITE);
                notification.setTextOrigin(VPos.CENTER);
                notification.setTextAlignment(TextAlignment.CENTER);
                notificationArea.getChildren().add(notification);

                notification.setText("Preparing To Install...");
                install.setStage(stage);
            });


        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static void doUpdateTest(){
        CompletableFuture.runAsync(()->{
            try {
                Thread.sleep(500);
                System.out.println("!!!");
                MiscUtils.updateLauncher();
            } catch (Exception e){
                e.printStackTrace();
            }
        });
    }

}
