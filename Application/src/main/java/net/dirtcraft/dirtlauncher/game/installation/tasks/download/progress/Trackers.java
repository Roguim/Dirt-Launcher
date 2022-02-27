package net.dirtcraft.dirtlauncher.game.installation.tasks.download.progress;

import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;

public class Trackers {
    public static MultiUpdater getTracker(ProgressContainer progressContainer, String calculating, String downloading){
        return new MultiUpdater() {
            DownloadUpdater downloadUpdater = getDlTracker(progressContainer, downloading);
            PreparationUpdater preparationUpdater = getPrepTracker(progressContainer, calculating);
            @Override
            public void sendUpdate(ProgressDetailed progress) {
                downloadUpdater.sendUpdate(progress);
            }

            @Override
            public void sendUpdate(ProgressBasic progress) {
                preparationUpdater.sendUpdate(progress);
            }
        };
    }

    public static MultiUpdater getSimpleTracker(ProgressContainer container, String type){
        return getTracker(container, "Fetching " + type, "Downloading " + type);
    }

    public static PreparationUpdater getPrepTracker(ProgressContainer progressContainer, String calculating){
        return progress -> {
            progressContainer.setMinorPercent(progress.getStageCompletionPercent());
            if (progress.isNthUpdate(5)) progressContainer.setProgressText(calculating, String.format("%s", progress.getStageRemainingFraction()));
        };
    }

    public static DownloadUpdater getDlTracker(ProgressContainer progressContainer, String downloading){
        return progress -> {
            progressContainer.setMinorPercent(progress.getStageCompletionPercent());
            if (progress.isNthUpdate(5)) {
                String info = String.format("%s (%s)", progress.getStageRemainingFraction(), progress.getFormattedBitRate());
                progressContainer.setProgressText(downloading, info);
            }
        };
    }

    public interface PreparationUpdater {
        void sendUpdate(ProgressBasic progress);
    }

    public interface DownloadUpdater {
        void sendUpdate(ProgressDetailed progress);
    }

    public interface MultiUpdater extends PreparationUpdater, DownloadUpdater{}

}
