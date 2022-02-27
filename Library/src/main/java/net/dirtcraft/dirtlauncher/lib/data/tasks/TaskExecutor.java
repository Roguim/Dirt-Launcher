package net.dirtcraft.dirtlauncher.lib.data.tasks;

import net.dirtcraft.dirtlauncher.lib.data.tasks.renderers.Renderer;
import net.dirtcraft.dirtlauncher.lib.util.BitRateSmoother;
import net.dirtcraft.dirtlauncher.lib.util.Util;

import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TaskExecutor {
    private static final Timer scheduler = new Timer();

    public static void prepare(Collection<? extends Task> tasks, Renderer onTick, long tickMs) {
        List<CompletableFuture<?>> futures = tasks.stream()
                .map(Task::preExecute)
                .collect(Collectors.toList());
        TimerTask render = Util.timerTask(()->{
            long completed = futures.stream().filter(CompletableFuture::isDone).count();
            onTick.apply("Preparing", completed, futures.size(), 0);
        });
        scheduler.scheduleAtFixedRate(render, 0, tickMs);
        while (futures.stream().allMatch(CompletableFuture::isDone)) Util.spin(500);
        render.cancel();
        Util.spin(tickMs + 5);
    }

    public static void execute(Collection<? extends Task> tasks, Renderer onTick, int tickMs, String title) {
        BitRateSmoother smoother = new BitRateSmoother(20);
        long target = tasks.stream().mapToLong(Task::getCompletion).sum();
        List<CompletableFuture<?>> futures = tasks.stream()
                .map(Task::execute)
                .collect(Collectors.toList());
        TimerTask render = Util.timerTask(()->{
            long completed = tasks.stream().mapToLong(Task::getProgress).sum();
            long bitRate = smoother.getAveraged(tasks.stream().mapToLong(Task::pollProgress).sum());
            if (onTick != null) onTick.apply(title, completed, target, bitRate);
        });
        scheduler.scheduleAtFixedRate(render, 0, tickMs);
        while (!futures.stream().allMatch(CompletableFuture::isDone)) Util.spin(500);
        render.cancel();
        Util.spin(tickMs + 5);
    }
}
