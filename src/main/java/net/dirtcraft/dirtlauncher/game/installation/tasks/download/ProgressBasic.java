package net.dirtcraft.dirtlauncher.game.installation.tasks.download;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class ProgressBasic {
    protected State state;
    protected final int updateCount;
    protected final int files;
    protected final long progress;

    ProgressBasic(Download[] downloads, int updateCount){
        this.state = State.DOWNLOADING;
        this.progress = Arrays.stream(downloads).mapToLong(Download::getProgress).sum();
        this.files = downloads.length;
        this.updateCount = updateCount;
    }

    ProgressBasic(AtomicInteger progress, int files, int updateCount){
        this.state = State.DOWNLOADING;
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

    public boolean isCalculating(){
        return state == State.CALCULATING;
    }

    public boolean isDownloading(){
        return state == State.DOWNLOADING;
    }

    public double getStageCompletionPercent() {
        return progress / (double) files;
    }

    public String getStageCompletionFraction() {
        return String.format("%d/%d", progress, files);
    }

    public boolean isComplete(){
        return updateCount == files;
    }

    protected enum State {
        CALCULATING,
        DOWNLOADING;
    }
}
