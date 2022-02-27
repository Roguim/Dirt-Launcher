package net.dirtcraft.dirtlauncher.lib.data.json.mojang.Java;

import com.google.gson.annotations.SerializedName;
import net.dirtcraft.dirtlauncher.lib.data.json.mojang.FileDownload;
import net.dirtcraft.dirtlauncher.lib.parsing.JsonUtils;
import org.apache.commons.lang3.SystemUtils;

import java.util.Map;
import java.util.Optional;

public class JavaManifest {
    private static final String OS_KEY = getKey();
    private static final String URL = "https://launchermeta.mojang.com/v1/products/java-runtime/2ec0cc96c44e5a76b9c8b7c39df7210883d12871/all.json";
    @SerializedName("linux-i386")
    Map<String, Entry[]> linux_86;
    @SerializedName("linux")
    Map<String, Entry[]> linux_64;
    @SerializedName("windows-x86")
    Map<String, Entry[]> win_86;
    @SerializedName("windows-x64")
    Map<String, Entry[]> win_64;
    @SerializedName("mac-os")
    Map<String, Entry[]> mac;

    public static Optional<JavaManifest> getManifest() {
        try {
            return Optional.of(JsonUtils.parseJsonOnline(URL, JavaManifest.class));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<JavaVersionManifest> getVersionManifest(String type) {
        Entry[] entries = getEntry(type);
        if (entries == null || entries.length == 0) return Optional.empty();
        Entry entry = entries[0]; //todo figure out why its an array lol
        return entry.getVersionManifest();
    }

    private Entry[] getEntry(String type) {
        Map<String, Entry[]> systemSpecific;
        switch (OS_KEY) {
            case "linux-i386":
                systemSpecific = linux_86;
                break;
            case "linux":
                systemSpecific = linux_64;
                break;
            case "windows-x86":
                systemSpecific = win_86;
                break;
            case "windows-x64":
                systemSpecific = win_64;
                break;
            default:
                systemSpecific = mac;
                break;
        }
        return systemSpecific.get(type);
    }

    private static String getKey() {
        String model = System.getProperty("sun.arch.data.model", System.getProperty("com.ibm.vm.bitmode"));
        if (model == null) model = System.getProperty("sun.arch.data.model");
        //if (model == null) Logger.INSTANCE.error("os arch null @ JavaManifest.class");
        boolean isX64 = "64".equals(model);
        if (SystemUtils.IS_OS_MAC) {
            return "mac-os";
        } else if (SystemUtils.IS_OS_LINUX) {
            return isX64? "linux" : "linux-i386";
        } else if (SystemUtils.IS_OS_WINDOWS) {
            return isX64? "windows-x64" : "windows-x86";
        } else return "";
    }

    public static class Entry{
        private FileDownload manifest;

        public Optional<JavaVersionManifest> getVersionManifest() {
            try {
                return Optional.of(JsonUtils.parseJsonOnline(manifest.url, JavaVersionManifest.class));
            } catch (Exception e) {
                return Optional.empty();
            }
        }
    }
}


