package net.dirtcraft.dirtlauncher.backend.components;

import javafx.application.Platform;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.backend.config.Constants;
import net.dirtcraft.dirtlauncher.elements.Pack;
import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;
import org.apache.commons.lang3.SystemUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.util.Optional;

public class SystemTray {

    public static java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();

    private static TrayIcon icon = null;

    public static Optional<TrayIcon> getIcon() {
        if (icon == null) return Optional.empty();
        return Optional.of(icon);
    }

    public static void createIcon(Pack pack) {
        try {
            // ensure awt toolkit is initialized.
            Toolkit.getDefaultToolkit();

            // app requires system tray support, just exit if there is no support.

            if (!java.awt.SystemTray.isSupported()) {
                System.out.println("No system tray support. Returning method.");
                return;
            }

            Image trayImage = ImageIO.read(MiscUtils.getResourceStream(Constants.ICONS, SystemUtils.IS_OS_WINDOWS ? "dirticon_tiny.png" : "dirticon_small.png"));

            // set up a system tray icon.

            TrayIcon trayIcon;
            if (getIcon().isPresent()) trayIcon = getIcon().get();
            else trayIcon = new TrayIcon(trayImage);

            trayIcon.setImageAutoSize(true);

            // if the user double-clicks on the tray icon, show the main stage
            trayIcon.addActionListener(event -> Platform.runLater(() -> Main.getInstance().getStage().show()));
            // set tooltip on hover
            trayIcon.setToolTip("Playing " + pack.getName());

            MenuItem exit = new MenuItem("Close");
            // the convention for tray icons seems to be to set the default icon for opening
            // the application stage in a bold font.
            exit.setFont(Font.decode(null).deriveFont(Font.BOLD));

            // to really exit the application, the user must go to the system tray icon
            // and select the exit option, this will shutdown JavaFX and remove the
            // tray icon (removing the tray icon will also shut down AWT).
            exit.addActionListener(event -> {
                Platform.exit();
                tray.remove(trayIcon);
            });


            // setup the popup menu for the application.
            final PopupMenu popup = new PopupMenu();
            popup.add(exit);
            trayIcon.setPopupMenu(popup);

            // add the application tray icon to the system tray.
            tray.add(trayIcon);

            // Display notification message
            trayIcon.displayMessage(pack.getName(), "Game Launching", TrayIcon.MessageType.INFO);

            SystemTray.icon = trayIcon;

        } catch (AWTException | IOException e) {
            e.printStackTrace();
        }
    }

}
