package net.dirtcraft.dirtlauncher.game.installation.tasks.download.progress;

public interface IProgress {
    boolean isNthUpdate(int n);

    int getUpdateCount();

    boolean isCalculating();

    boolean isDownloading();

    double getStageCompletionPercent();

    String getStageCompletionFraction();

    String getStageRemainingFraction();

    boolean isComplete();

    enum State {
        CALCULATING,
        DOWNLOADING;
    }
}
