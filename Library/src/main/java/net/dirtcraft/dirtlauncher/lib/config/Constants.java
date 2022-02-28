package net.dirtcraft.dirtlauncher.lib.config;

import net.dirtcraft.dirtlauncher.lib.DirtLib;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class Constants {
    public static final CompletableFuture<?> COMPLETED_FUTURE = CompletableFuture.completedFuture(null);
    public static final Path DIR_RUNTIMES = DirtLib.LAUNCHER_DIR.resolve("Runtime");
    public static final Path DIR_CONFIG = DirtLib.LAUNCHER_DIR.resolve("Config");
    public static final Path DIR_TEMP = DirtLib.LAUNCHER_DIR.resolve("temp");

    public static final String LAUNCHERMETA_JAVA_URL = "https://launchermeta.mojang.com/v1/products/java-runtime/2ec0cc96c44e5a76b9c8b7c39df7210883d12871/all.json";
    public static final String CURSE_API_URL = "https://addons-ecs.forgesvc.net/api/v2/addon/";
    public static final String PACK_JSON_URL = "http://164.132.201.67/launcher/packs.json";
    public static final String UPDATE_URL = "http://164.132.201.67/launcher/Dirt-Launcher.jar";
    public static final String MICROSOFT_LOGIN_REDIRECT_SUFFIX = "https://login.live.com/oauth20_desktop.srf?code=";
    public static final String MICROSOFT_LOGIN_URL = "https://login.live.com/oauth20_authorize.srf" +
            "?client_id=00000000402b5328" +
            "&response_type=code" +
            "&scope=service%3A%3Auser.auth.xboxlive.com%3A%3AMBI_SSL" +
            "&redirect_uri=https%3A%2F%2Flogin.live.com%2Foauth20_desktop.srf";
}
