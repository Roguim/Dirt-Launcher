package net.dirtcraft.dirtlauncher.game.installation.tasks.download.progress;

import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.DownloadTask;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class ProgressBasic {
    protected final int updateCount;
    protected final int files;
    protected final long progress;

    public ProgressBasic(DownloadTask[] downloads, int updateCount){
        this.progress = Arrays.stream(downloads).mapToLong(DownloadTask::getProgress).sum();
        this.files = downloads.length;
        this.updateCount = updateCount;
    }

    public ProgressBasic(AtomicInteger progress, int files, int updateCount){
        this.files = files;
        this.progress = progress.get();
        this.updateCount = updateCount;
    }

    public boolean isNthUpdate(int n) {
        return updateCount % n == 0;
    }

    public int getUpdateCount(){
        return updateCount;
    }

    public double getStageCompletionPercent() {
        return progress / (double) files;
    }

    public String getStageCompletionFraction() {
        return String.format("%d/%d", progress, files);
    }

    public String getStageRemainingFraction() {
        return String.format("%d/%d", files - progress, files);
    }

    public boolean isComplete(){
        return updateCount == files;
    }
}
