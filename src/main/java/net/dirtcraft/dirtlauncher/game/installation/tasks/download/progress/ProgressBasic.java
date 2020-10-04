package net.dirtcraft.dirtlauncher.game.installation.tasks.download.progress;

import net.dirtcraft.dirtlauncher.game.installation.tasks.download.DataFormat;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.DownloadTask;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class ProgressBasic implements IProgress {
    protected State state;
    protected final int updateCount;
    protected final int files;
    protected final long progress;

    public ProgressBasic(DownloadTask[] downloads, int updateCount){
        this.state = State.DOWNLOADING;
        this.progress = Arrays.stream(downloads).mapToLong(DownloadTask::getProgress).sum();
        this.files = downloads.length;
        this.updateCount = updateCount;
    }

    public ProgressBasic(AtomicInteger progress, int files, int updateCount){
        this.state = State.DOWNLOADING;
        this.files = files;
        this.progress = progress.get();
        this.updateCount = updateCount;
    }

    @Override
    public boolean isNthUpdate(int n) {
        return updateCount % n == 0;
    }

    @Override
    public int getUpdateCount(){
        return updateCount;
    }

    @Override
    public boolean isCalculating(){
        return state == State.CALCULATING;
    }

    @Override
    public boolean isDownloading(){
        return state == State.DOWNLOADING;
    }

    @Override
    public double getStageCompletionPercent() {
        return progress / (double) files;
    }

    @Override
    public String getStageCompletionFraction() {
        return String.format("%d/%d", progress, files);
    }

    public String getStageRemainingFraction() {
        return String.format("%d/%d", files - progress, files);
    }

    @Override
    public boolean isComplete(){
        return updateCount == files;
    }
}
