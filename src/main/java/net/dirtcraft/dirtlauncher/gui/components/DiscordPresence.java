package net.dirtcraft.dirtlauncher.gui.components;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import net.dirtcraft.dirtlauncher.configuration.Constants;
import net.dirtcraft.dirtlauncher.logging.Logger;

public class DiscordPresence {

    private static final String applicationId = "598965613767032833";

    private static DiscordRPC rpc = DiscordRPC.INSTANCE;

    private static DiscordRichPresence presence;
    private static DiscordEventHandlers handlers;

    public static void setState(String state) {
        getPresence().state = state;
        Logger.INSTANCE.debug("Discord Rich Presence STATE set to \"" + state + "\"");
        refreshPresence();
    }

    public static void setDetails(String details) {
        getPresence().details = details;
        Logger.INSTANCE.debug("Discord Rich Presence DETAILS set to \"" + details + "\"");
        refreshPresence();
    }

    private static void refreshPresence() {
        rpc.Discord_UpdatePresence(getPresence());
    }

    public static void initPresence() {
        rpc.Discord_Initialize(applicationId, getHandlers(), true, null);

        refreshPresence();

        shutdownHook();
    }

    private static DiscordRichPresence getPresence() {
        if (presence != null) return presence;
        DiscordRichPresence presence = new DiscordRichPresence();
        presence.startTimestamp = System.currentTimeMillis() / 1000;
        presence.largeImageKey = "dirticon_crisp";
        rpc.Discord_UpdatePresence(presence);

        DiscordPresence.presence = presence;

        return presence;
    }

    private static DiscordEventHandlers getHandlers() {
        if (handlers != null) return handlers;
        DiscordEventHandlers handlers = new DiscordEventHandlers();

        if (Constants.DEBUG) {
            handlers.ready = (user) -> System.out.println("Detected Discord Account: @" + user.username + "#" + user.discriminator);
        }

        rpc.Discord_UpdateHandlers(handlers);
        DiscordPresence.handlers = handlers;

        return handlers;
    }

    private static void shutdownHook() {
        Thread thread = new Thread(() -> {
            rpc.Discord_ClearPresence();
            rpc.Discord_Shutdown();
        }, "Discord-RPC-Shutdown");
        thread.setDaemon(true);
        Runtime.getRuntime().addShutdownHook(thread);
    }

}
