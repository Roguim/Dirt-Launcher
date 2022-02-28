package net.dirtcraft.dirtlauncher.configuration;

import net.dirtcraft.dirtlauncher.DirtLauncher;

public class Constants {
    public static final String LAUNCHER_VERSION = "@VERSION@";
    public static final String BOOTSTRAP_JAR = "@BOOTSTRAP@";

    public static final String AUTHORS = "ShinyAfro, Julian & TechDG";
    public static final String HELPERS = "Lordomus";


    public static final String DEFAULT_JAVA_ARGS = "-XX:+UseG1GC -Dfml.readTimeout=180 -Dsun.rmi.dgc.server.gcInterval=2147483646 -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M";

    public static final String CSS_CLASS_VBOX = "v-box";
    public static final String CSS_CLASS_TITLE = "Title";
    public static final String CSS_CLASS_TEXT = "text";
    public static final String CSS_CLASS_INDICATOR = "indicator";
    public static final String CSS_ID_ROOT = "Root";

    public static final String CSS_ID_PACKLIST_BG = "PackListBG";
    public static final String CSS_ID_LOGIN_BAR = "LoginBar";
    public static final String CSS_ID_TOOLBAR_UPPER = "ToolBarUpper";
    public static final String CSS_ID_TOOLBAR_LOWER = "ToolBarLower";
    public static final String CSS_ID_SETTINGS_GMDR = "GameLocation";

    public final static String JAR_CSS_FXML = "CSS/FXML";
    public final static String JAR_CSS_HTML = "CSS/HTML";
    public static final String JAR_PACK_IMAGES = "Images/Packs";
    public final static String JAR_FONTS = "CSS/Fonts";
    public final static String JAR_BACKGROUNDS = "Backgrounds";
    public final static String JAR_SCENES = "Scenes";
    public final static String JAR_IMAGES = "Images";
    public final static String JAR_ICONS = "Icons";

    @SuppressWarnings("ConstantConditions") //Tokens will replace this on jfxjar / build, but will not when ran using IntelliJ's IDEA application run build script.
    public static final boolean DEBUG = (DirtLauncher.getOptions().contains("-debug") || LAUNCHER_VERSION.equals('@' + "VERSION" + '@'));
    public static final boolean VERBOSE = (DirtLauncher.getOptions().contains("-verbose"));

    public static final int MAX_DOWNLOAD_ATTEMPTS = 8;
    public static final int MAX_DOWNLOAD_THREADS = 24;

}
