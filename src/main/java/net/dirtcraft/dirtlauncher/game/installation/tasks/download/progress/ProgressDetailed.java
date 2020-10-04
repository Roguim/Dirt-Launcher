package net.dirtcraft.dirtlauncher.game.installation.tasks.download.progress;

import net.dirtcraft.dirtlauncher.game.installation.tasks.download.DataFormat;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.DownloadManager;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.DownloadTask;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class ProgressDetailed extends ProgressBasic {
    private final long totalSize;
    private final long bytesPerSecond;

    public ProgressDetailed(DownloadTask[] downloads, DownloadManager.BitrateSmoother bitrateSmoother, int updateCount) {
        super(downloads, updateCount);
        this.bytesPerSecond = bitrateSmoother.getAveraged(Arrays.stream(downloads).mapToLong(DownloadTask::getBytesPerSecond).sum());
        this.totalSize = Arrays.stream(downloads).mapToLong(DownloadTask::getSize).sum();
    }

    public ProgressDetailed(AtomicInteger progress, int files, int updateCount) {
        super(progress, files, updateCount);
        this.state = State.CALCULATING;
        this.bytesPerSecond = 0;
        this.totalSize = 0;
    }

    @Override
    public double getStageCompletionPercent() {
        if (state == State.CALCULATING) return super.getStageCompletionPercent();
        return progress / (double) totalSize;
    }

    @Override
    public String getStageCompletionFraction() {
        if (state == State.CALCULATING) return super.getStageCompletionFraction();
        return String.format("%s/%s", getFormattedRemaining(), getFormattedSize());
    }

    @Override
    public String getStageRemainingFraction() {
        if (state == State.CALCULATING) return super.getStageRemainingFraction();
        DataFormat format = DataFormat.getMaximumDataRate(totalSize);
        final double remaining = totalSize == progress? 0 : (double) (totalSize - progress) / format.getBytes();
        return String.format("%.1f/%s", remaining, format.toFileSize(totalSize));
    }

    @Override
    public boolean isComplete(){
        if (state == State.CALCULATING) return super.isComplete();
        return progress == totalSize;
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

}
