package net.dirtcraft.dirtlauncher.backend.components;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;

public class DiscordPresence {

    private static DiscordRichPresence presence;

    public static DiscordRichPresence getPresence() {
        if (presence == null) initPresence();
        return initPresence();
    }

    public static void setStatus(String status) {
        getPresence().details = status;
    }


    public static DiscordRichPresence initPresence() {
        DiscordRPC lib = DiscordRPC.INSTANCE;
        String applicationId = "598965613767032833";
        DiscordEventHandlers handlers = new DiscordEventHandlers();
        handlers.ready = (user) ->
                System.out.println("Detected Discord Account: @" + user.username + "#" + user.discriminator);

        lib.Discord_Initialize(applicationId, handlers, true, null);
        DiscordRichPresence presence = new DiscordRichPresence();
        presence.startTimestamp = System.currentTimeMillis() / 1000; // epoch second
        presence.details = "Testing RPC";
        lib.Discord_UpdatePresence(presence);
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                lib.Discord_RunCallbacks();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {}
            }
        }, "RPC-Callback-Handler").start();

        return presence;
    }

}
