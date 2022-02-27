package net.dirtcraft.dirtlauncher.lib.config;

import net.dirtcraft.dirtlauncher.lib.DirtLib;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class Constants {
    public static final CompletableFuture<?> COMPLETED_FUTURE = CompletableFuture.completedFuture(null);
    public static final Path DIR_RUNTIMES = DirtLib.LAUNCHER_DIR.resolve("Runtime");
    public static final Path DIR_CONFIG = DirtLib.LAUNCHER_DIR.resolve("Config");
    public static final Path DIR_TEMP = DirtLib.LAUNCHER_DIR.resolve("temp");
}
