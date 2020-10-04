package net.dirtcraft.dirtlauncher.game.installation.tasks.download.progress;

import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.logging.Logger;

import java.util.function.Consumer;

public class Trackers {
    public static Consumer<ProgressDetailed> getProgressContainerTracker(ProgressContainer progressContainer, String calculating, String downloading){
        return progress -> {
            try {
                progressContainer.setMinorPercent(progress.getStageCompletionPercent());
                if (!progress.isNthUpdate(5)) return;
                else if (progress.state == ProgressBasic.State.CALCULATING) progressContainer.setProgressText(calculating, String.format("%s", progress.getStageRemainingFraction()));
                else progressContainer.setProgressText(downloading, String.format("%s (%s)", progress.getStageRemainingFraction(), progress.getFormattedBitRate()));
            } catch (Throwable e){
                Logger.INSTANCE.error(e);
            }
        };
    }

}
