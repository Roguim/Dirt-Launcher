package net.dirtcraft.dirtlauncher.game.installation;

import javafx.application.Platform;
import javafx.scene.text.Text;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IInstallationTask;
import net.dirtcraft.dirtlauncher.gui.wizards.Install;

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

    public void setProgressText(String text) {
        updateUI(install -> ((Text)install.getNotificationText().getChildren().get(0)).setText(text + "..."));
    }

    public void completeMajorStep() {
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

    public void setMinorStepsCompleted(int setMinorStepsCompleted) {
        minorStepsCompleted.set(setMinorStepsCompleted);
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

}
