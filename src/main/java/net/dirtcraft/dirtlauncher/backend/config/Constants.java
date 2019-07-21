package net.dirtcraft.dirtlauncher.backend.config;

import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;

public class Constants {
    public static final String CSS_PACKLIST = "PackList";
    public static final String CSS_PACK_CELL = "PackCell";
    public static final String CSS_PACK_MENU = "PackCellContext";
    public static final String CSS_PACK_MENU_OPTION = "PackCellContextOption";

    public static final boolean VERBOSE = MiscUtils.inIde();

    public static final String LAUNCHER_VERSION = "1.0.1";
    public static final String DEFAULT_JAVA_ARGS = "-XX:+UseG1GC -Dfml.readTimeout=180 -Dsun.rmi.dgc.server.gcInterval=2147483646 -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M";


    public final static String CSS_FXML = "CSS/FXML";
    public final static String CSS_HTML = "CSS/HTML";
    public static final String PACK_IMAGES = "Images/Packs";
    public final static String FONTS = "CSS/Fonts";
    public final static String BACKGROUNDS = "Backgrounds";
    public final static String SCENES = "Scenes";
    public final static String IMAGES = "Images";
    public final static String ICONS = "Icons";
}
