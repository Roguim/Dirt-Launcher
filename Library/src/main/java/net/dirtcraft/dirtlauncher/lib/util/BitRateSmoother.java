package net.dirtcraft.dirtlauncher.lib.util;

import java.util.Arrays;

public class BitRateSmoother {
    private final long[] bytesPerSecond;
    private final int samples;
    private int counter;
    public BitRateSmoother(int samples){
        this.counter = 0;
        this.bytesPerSecond = new long[samples];
        Arrays.fill(bytesPerSecond, 0);
        this.samples = samples;
    }

    public long getAveraged(long i){
        int count = counter == Integer.MAX_VALUE? 0 : counter++;
        bytesPerSecond[count % samples] = i;
        return (long) Arrays.stream(bytesPerSecond).average().orElse(0d);
    }
}
