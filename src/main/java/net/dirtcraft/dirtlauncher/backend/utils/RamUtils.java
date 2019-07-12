package net.dirtcraft.dirtlauncher.backend.utils;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;

public class RamUtils {

    private static final long maxMemory = (((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize()) / 1024 / 1024;

    public static int getRecommendedRam() {

        if (maxMemory > 10000) return 8;
        else if (maxMemory > 7000) return 6;
        else if (maxMemory > 5000) return 4;
        else if (maxMemory > 2000) return 3;
        else return 2;
    }

    public static int getMinimumRam() {

        if (maxMemory > 5000) return 4;
        else if (maxMemory > 3000) return 3;
        else return 2;
    }

    public static long getMegabytes() {
        return maxMemory;
    }

    public static long getGigabytes() {
        return maxMemory / 1024;
    }


}
