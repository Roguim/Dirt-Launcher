package net.dirtcraft.dirtlauncher.utils;

import net.dirtcraft.dirtlauncher.Main;

public class Constants {
    public static final String LAUNCHER_VERSION = "1.0.1";
    public static final String AUTHORS = "Shinyafro, Julian & TechDG";
    public static final String HELPERS = "Lordomus";

    public static final String UPDATE_URL = "https://dirtcraft.net/launcher/Dirt-Launcher.jar";
    public static final String PACK_JSON_URL = "http://164.132.201.67/launcher/packs.json";

    public static final String DEFAULT_JAVA_ARGS = "-XX:+UseG1GC -Dfml.readTimeout=180 -Dsun.rmi.dgc.server.gcInterval=2147483646 -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M";

    public static final String CSS_CLASS_VBOX = "v-box";
    public static final String CSS_CLASS_TITLE = "Title";

    public static final String CSS_ID_PACKLIST_BG = "PackListBG";
    public static final String CSS_ID_LOGIN_BAR = "LoginBar";
    public static final String CSS_ID_TOOLBAR_UPPER = "ToolBarUpper";
    public static final String CSS_ID_TOOLBAR_LOWER = "ToolBarLower";

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
