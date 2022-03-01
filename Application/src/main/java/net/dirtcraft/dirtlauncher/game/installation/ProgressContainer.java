package net.dirtcraft.dirtlauncher.game.installation;

import javafx.application.Platform;
import javafx.scene.text.Text;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.gui.wizards.Install;
import net.dirtcraft.dirtlauncher.lib.data.tasks.renderers.Renderer;
import net.dirtcraft.dirtlauncher.lib.util.DataFormat;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ProgressContainer {

    private final int numMajorSteps;
    private int majorStepsCompleted = -1;

    private int numMinorSteps = 0;
    private AtomicInteger minorStepsCompleted = new AtomicInteger(0);

    public ProgressContainer(Collection<IInstallationTask> installTasks) {
        setProgressText("Preparing");
        setNumMinorSteps(1);

        numMajorSteps = installTasks.stream()
                .mapToInt(IInstallationTask::getNumberSteps)
                .sum();
    }

    public ProgressContainer(){
        setProgressText("Preparing");
        setNumMinorSteps(1);
        numMajorSteps = 1;
    }

    public void setProgressText(String text) {
        updateUI(install -> ((Text)install.getNotificationText().getChildren().get(0)).setText(text + "..."));
    }

    public void setProgressText(String text, String progress) {
        updateUI(install -> ((Text)install.getNotificationText().getChildren().get(0)).setText(text + "...\n" + progress));
    }

    public void nextMajorStep(String text) {
        majorStepsCompleted++;
        updateUI(install -> install.getBottomBar().setProgress(((double)majorStepsCompleted) / numMajorSteps));
        setProgressText(text);
    }

    public void nextMajorStep(String text, int steps) {
        majorStepsCompleted++;
        updateUI(install -> install.getBottomBar().setProgress(((double)majorStepsCompleted) / numMajorSteps));
        setProgressText(text);
        setNumMinorSteps(steps);
    }

    public void nextMajorStep() {
        majorStepsCompleted++;
        updateUI(install -> install.getBottomBar().setProgress(((double)majorStepsCompleted) / numMajorSteps));
    }

    public void setNumMinorSteps(int numMinorSteps) {
        this.numMinorSteps = numMinorSteps;
        minorStepsCompleted = new AtomicInteger(0);
        updateMinorStepsUI();
    }

    public void completeMinorStep() {
        minorStepsCompleted.incrementAndGet();
        updateMinorStepsUI();
    }

    public void setMinorPercent(double percent){
        updateUI(install -> install.getLoadingBar().setProgress(percent));
    }

    public void setMinorStepsCompleted(int setMinorStepsCompleted) {
        minorStepsCompleted.set(setMinorStepsCompleted);
        updateMinorStepsUI();
    }

    public void addMinorStepsCompleted(int minorStepsCompleted) {
        this.minorStepsCompleted.addAndGet(minorStepsCompleted);
        updateMinorStepsUI();
    }

    private void updateMinorStepsUI() {
        updateUI(install -> install.getLoadingBar().setProgress(((double)minorStepsCompleted.get()) / numMinorSteps));
    }

    private void updateUI(Consumer<Install> consumer) {
        Platform.runLater(() -> Install.getInstance().ifPresent(consumer));
    }

    public Renderer.ProgressRenderer showProgress() {
        completeMinorStep();
        return this::progress;
    }

    public Renderer.BitrateRenderer showBitrate() {
        completeMinorStep();
        return this::bitrate;
    }

    private void progress(String title, long tasksCompleted, long tasksTotal, double percent) {
        setMinorPercent(percent);
        String info = String.format("%s/%s",
                tasksCompleted,
                tasksTotal);
        setProgressText(title, info);
    };

    private void bitrate(String title, long bytesCompleted, long bytesTotal, long bitrate, double percent) {
        setMinorPercent(percent);
        DataFormat format = DataFormat.getMaximumDataRate(bytesTotal);
        String info = String.format("%s/%s (%s)",
                format.toFileSize(bytesCompleted),
                format.toFileSize(bytesTotal),
                DataFormat.getBitrate(bitrate));
        setProgressText(title, info);
    };

}
