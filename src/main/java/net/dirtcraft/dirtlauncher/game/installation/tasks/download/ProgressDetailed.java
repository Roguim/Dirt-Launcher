package net.dirtcraft.dirtlauncher.game.installation.tasks.download;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class ProgressDetailed extends ProgressBasic {
    private final long totalSize;
    private final long bytesPerSecond;

    ProgressDetailed(Download[] downloads, DownloadManager.BitrateSmoother bitrateSmoother, int updateCount) {
        super(downloads, updateCount);
        this.bytesPerSecond = bitrateSmoother.getAveraged(Arrays.stream(downloads).mapToLong(Download::getBytesPerSecond).sum());
        this.totalSize = Arrays.stream(downloads).mapToLong(Download::getSize).sum();
    }

    ProgressDetailed(AtomicInteger progress, int files, int updateCount) {
        super(progress, files, updateCount);
        this.state = State.CALCULATING;
        this.bytesPerSecond = 0;
        this.totalSize = 0;
    }

    @Override
    public double getStageCompletionPercent() {
        switch (state) {
            case CALCULATING: return progress / (double) files;
            case DOWNLOADING: return progress / (double) totalSize;
            default: return 0d;
        }
    }

    @Override
    public String getStageCompletionFraction() {
        switch (state) {
            case CALCULATING: return String.format("%d/%d", progress, files);
            case DOWNLOADING: return String.format("%s/%s", getFormattedRemaining(), getFormattedSize());
            default: return "";
        }
    }

    public String getFormattedBitRate() {
        if (state == State.CALCULATING) return "0B/s";
        return DataFormat.getBitrate(bytesPerSecond);
    }

    public long getBitrate() {
        return bytesPerSecond;
    }

    public String getFormattedSize() {
        if (state == State.CALCULATING) return "Unknown";
        return DataFormat.getFileSize(totalSize);
    }

    public long getSize() {
        if (state == State.CALCULATING) return 0;
        return totalSize;
    }

    public String getFormattedDownloaded() {
        if (state == State.CALCULATING) return "0MB";
        return DataFormat.getFileSize(progress);
    }

    public long getDownloaded() {
        if (state == State.CALCULATING) return 0;
        return progress;
    }

    public String getFormattedRemaining(){
        if (state == State.CALCULATING) return "Unknown";
        return DataFormat.getFileSize(totalSize-progress);
    }

    public long getRemaining(){
        if (state == State.CALCULATING) return 0;
        return totalSize-progress;
    }

    @Override
    public boolean isComplete(){
        if (state == State.CALCULATING) return super.isComplete();
        return progress == totalSize;
    }

}
