package net.dirtcraft.dirtlauncher.gui.components;

import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.dirtcraft.dirtlauncher.configuration.Constants;
import net.dirtcraft.dirtlauncher.logging.Logger;

public class DiscordPresence {

    private static final String applicationId = "598965613767032833";

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
        DiscordRPC.discordUpdatePresence(getPresence());
    }

    public static void initPresence() {
        DiscordRPC.discordInitialize(applicationId, getHandlers(), true);
        Logger.INSTANCE.info("Initializing Discord Rich Presence...");

        refreshPresence();

        shutdownHook();
    }

    private static DiscordRichPresence getPresence() {
        if (presence != null) return presence;
        DiscordRichPresence presence = new DiscordRichPresence();
        presence.startTimestamp = System.currentTimeMillis() / 1000;
        presence.largeImageKey = "dirticon_crisp";
        DiscordRPC.discordUpdatePresence(presence);

        DiscordPresence.presence = presence;

        return presence;
    }

    private static DiscordEventHandlers getHandlers() {
        if (handlers != null) return handlers;
        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().setReadyEventHandler(user ->
                Logger.INSTANCE.debug("Discord Rich Presensce handlers registered.")).build();

        if (Constants.DEBUG) handlers.ready = user -> System.out.println("Detected Discord Account: @" + user.username + "#" + user.discriminator);

        DiscordRPC.discordUpdateEventHandlers(handlers);
        DiscordPresence.handlers = handlers;

        return handlers;
    }

    private static void shutdownHook() {
        Thread thread = new Thread(DiscordPresence::shutdown, "Discord-RPC-Shutdown");
        thread.setDaemon(true);
        Runtime.getRuntime().addShutdownHook(thread);
    }

    public static void shutdown() {
        DiscordRPC.discordClearPresence();
        DiscordRPC.discordShutdown();
    }

}
