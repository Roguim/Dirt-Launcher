package net.dirtcraft.dirtlauncher.game.installation.tasks.download;

import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;

import java.io.PrintStream;
import java.util.function.Consumer;

public class Trackers {
    public static Consumer<ProgressDetailed> getProgressContainerTracker(ProgressContainer progressContainer){
        return progress -> {
            progressContainer.setMinorPercent(progress.getStageCompletionPercent());
            if (!progress.isNthUpdate(5)) return;
            if (progress.isCalculating()) progressContainer.setProgressText(String.format("Calculating Downloads (%s)", progress.getStageCompletionFraction()));
            else progressContainer.setProgressText(String.format("Downloading %s at %s", progress.getFormattedSize(), progress.getFormattedBitRate()));
        };
    }

    public static Consumer<ProgressDetailed> getPrintStreamTracker(PrintStream printStream){
        return progress -> {
            if (!progress.isNthUpdate(5)) return;
            printStream.println(String.format("Downloading %s at %s (%s / %.1f%% remaining)", progress.getFormattedSize(), progress.getFormattedBitRate(), progress.getFormattedRemaining(), progress.getStageCompletionPercent()));
        };
    }

}
