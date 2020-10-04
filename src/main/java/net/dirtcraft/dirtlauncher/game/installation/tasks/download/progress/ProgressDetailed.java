package net.dirtcraft.dirtlauncher.game.installation.tasks.download.progress;

import net.dirtcraft.dirtlauncher.game.installation.tasks.download.DataFormat;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.DownloadManager;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.DownloadTask;

import java.util.Arrays;

public class ProgressDetailed extends ProgressBasic {
    private final long totalSize;
    private final long bytesPerSecond;

    public ProgressDetailed(DownloadTask[] downloads, DownloadManager.BitrateSmoother bitrateSmoother, int updateCount) {
        super(downloads, updateCount);
        this.bytesPerSecond = bitrateSmoother.getAveraged(Arrays.stream(downloads).mapToLong(DownloadTask::getBytesPerSecond).sum());
        this.totalSize = Arrays.stream(downloads).mapToLong(DownloadTask::getSize).sum();
    }

    @Override
    public double getStageCompletionPercent() {
        return progress / (double) totalSize;
    }

    @Override
    public String getStageCompletionFraction() {
        return String.format("%s/%s", getFormattedRemaining(), getFormattedSize());
    }

    @Override
    public String getStageRemainingFraction() {
        DataFormat format = DataFormat.getMaximumDataRate(totalSize);
        final double remaining = totalSize == progress? 0 : (double) (totalSize - progress) / format.getBytes();
        return String.format("%.1f/%s", remaining, format.toFileSize(totalSize));
    }

    @Override
    public boolean isComplete(){
        return progress == totalSize;
    }

    public String getFormattedBitRate() {
        return DataFormat.getBitrate(bytesPerSecond);
    }

    public long getBitrate() {
        return bytesPerSecond;
    }

    public String getFormattedSize() {
        return DataFormat.getFileSize(totalSize);
    }

    public long getSize() {
        return totalSize;
    }

    public String getFormattedDownloaded() {
        return DataFormat.getFileSize(progress);
    }

    public long getDownloaded() {
        return progress;
    }

    public String getFormattedRemaining(){
        return DataFormat.getFileSize(totalSize-progress);
    }

    public long getRemaining(){
        return totalSize-progress;
    }

}
