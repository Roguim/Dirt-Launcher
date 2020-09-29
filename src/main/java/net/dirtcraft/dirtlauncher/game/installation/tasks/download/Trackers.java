package net.dirtcraft.dirtlauncher.game.installation.tasks.download;

import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Trackers {
    private static int SAMPLES = 25;
    private static int PER_TEXT = 3;
    public static Consumer<DownloadManager.Progress> getProgressContainerTracker(ProgressContainer progressContainer, int bitrateSamples, int updatesPerText){
        AtomicInteger counter = new AtomicInteger();
        long[] bytesPerSecond = new long[bitrateSamples];
        Arrays.fill(bytesPerSecond, 0);
        return progress -> {
            int i = counter.addAndGet(1);
            bytesPerSecond[i % bitrateSamples] = progress.getBytesPerSecond();
            progressContainer.setMinorPercent(progress.getPercent());
            if (i % updatesPerText != 0 || progress.totalSize == 0) return;

            final long sampledSpeed = (long) Arrays.stream(bytesPerSecond).average().orElse(0d);
            final String speed = DataRates.getBitrate(sampledSpeed);
            final String size = DataRates.getFileSize(progress.totalSize);
            progressContainer.setProgressText(String.format("Downloading %s at %s", size, speed));
        };
    }

    public static Consumer<DownloadManager.Progress> getPrintStreamTracker(PrintStream printStream, int bitrateSamples, int updatesPerText){
        AtomicInteger counter = new AtomicInteger();
        long[] bytesPerSecond = new long[bitrateSamples];
        Arrays.fill(bytesPerSecond, 0);
        return progress -> {
            int i = counter.addAndGet(1);
            bytesPerSecond[i % bitrateSamples] = progress.getBytesPerSecond();
            if (i % updatesPerText != 0 || progress.totalSize == 0) return;

            final long sampledSpeed = (long) Arrays.stream(bytesPerSecond).average().orElse(0d);
            final String speed = DataRates.getBitrate(sampledSpeed);
            final String size = DataRates.getFileSize(progress.totalSize);
            final String remaining = DataRates.getFileSize(progress.totalSize - progress.progress);
            final double percent = progress.getPercent();
            printStream.println(String.format("Downloading %s at %s (%s / %.1f%% remaining)", size, speed, remaining, percent));
        };

    }
}
