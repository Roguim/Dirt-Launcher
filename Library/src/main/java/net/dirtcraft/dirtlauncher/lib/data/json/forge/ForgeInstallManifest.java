package net.dirtcraft.dirtlauncher.lib.data.json.forge;

import net.dirtcraft.dirtlauncher.lib.data.json.mojang.Library;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForgeInstallManifest {
    protected String minecraft;
    private List<ForgePostProcess.Processor> processors;
    protected Map<String, SidedFile> data;
    protected Library[] libraries;

    public Map<String, String> getData(boolean client) {
        Map<String, String> specific = new HashMap<>();
        if (data != null) this.data.forEach((k, v)->specific.put(k, client? v.client : v.server));
        return specific;
    }

    public ForgePostProcess getClientPostProcess() {
        return new ForgePostProcess(true, minecraft, processors, getData(true));
    }

    public ForgePostProcess getServerPostProcess() {
        return new ForgePostProcess(true, minecraft, processors, getData(false));
    }

    public String getMinecraft() {
        return minecraft;
    }

    public Library[] getLibraries() {
        return libraries == null? new Library[0] : libraries;
    }

    public static class SidedFile {
        private String client;
        private String server;
    }
}
