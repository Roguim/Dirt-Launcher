package net.dirtcraft.dirtlauncher.backend.utils;

import net.dirtcraft.dirtlauncher.Main;

public class Constants {
    public static final String LAUNCHER_VERSION = "1.0.1";
    public static final String DEFAULT_JAVA_ARGS = "-XX:+UseG1GC -Dfml.readTimeout=180 -Dsun.rmi.dgc.server.gcInterval=2147483646 -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M";

    public static final String CSS_CLASS_PACKLIST = "PackList";
    public static final String CSS_CLASS_PACK_CELL = "PackCell";
    public static final String CSS_CLASS_PACK_MENU = "PackCellContext";
    public static final String CSS_CLASS_PACK_MENU_OPTION = "PackCellContextOption";

    public final static String JAR_CSS_FXML = "CSS/FXML";
    public final static String JAR_CSS_HTML = "CSS/HTML";
    public static final String JAR_PACK_IMAGES = "Images/Packs";
    public final static String JAR_FONTS = "CSS/Fonts";
    public final static String JAR_BACKGROUNDS = "Backgrounds";
    public final static String JAR_SCENES = "Scenes";
    public final static String JAR_IMAGES = "Images";
    public final static String JAR_ICONS = "Icons";

    public static final boolean VERBOSE = (MiscUtils.inIde() || Main.getOptions().contains("-debug"));

}
