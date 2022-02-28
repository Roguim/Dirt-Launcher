package net.dirtcraft.dirtlauncher.lib.data.tasks;

import net.dirtcraft.dirtlauncher.lib.data.tasks.renderers.Renderer;
import net.dirtcraft.dirtlauncher.lib.util.BitRateSmoother;
import net.dirtcraft.dirtlauncher.lib.util.Util;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class TaskExecutor {
    private static final Timer scheduler = new Timer();
    public static  <T extends Task<?>> Collector<T, List<T>, List<T>> collector(Renderer onTick, String title){
        return new Collector<T, List<T>, List<T>>() {


            @Override
            public Supplier<List<T>> supplier() {
                return ArrayList::new;
            }

            @Override
            public BiConsumer<List<T>, T> accumulator() {
                return List::add;
            }

            @Override
            public BinaryOperator<List<T>> combiner() {
                return (left, right) -> { left.addAll(right); return left; };
            }

            @Override
            public Function<List<T>, List<T>> finisher() {
                return (a) -> {
                    execute(a, onTick, title);
                    return a;
                };
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH));
            }
        };
    }

    public static Collection<? extends Task<?>> prepare(Collection<? extends Task<?>> tasks, Renderer.ProgressRenderer onTick) {
        return prepare(tasks, onTick, 50);
    }

    public static Collection<? extends Task<?>> prepare(Collection<? extends Task<?>> tasks, Renderer.ProgressRenderer onTick, long tickMs) {
        List<CompletableFuture<?>> futures = tasks.stream()
                .map(Task::prepare)
                .collect(Collectors.toList());
        long tasksTotal = tasks.size();
        TimerTask render = Util.timerTask(()->{
            long tasksCompleted = futures.stream().filter(CompletableFuture::isDone).count();
            if (onTick != null) onTick.apply("Preparing", tasksCompleted, tasksTotal, 0, 0, 0);
        });
        scheduler.scheduleAtFixedRate(render, 0, tickMs);
        while (futures.stream().allMatch(CompletableFuture::isDone)) Util.spin(500);
        render.cancel();
        Util.spin(tickMs + 5);
        return tasks;
    }

    public static <T extends Task<?>> Collection<T> execute(Collection<T> tasks, Renderer onTick) {
        String title = tasks.stream().findFirst().map(Task::getType).orElse("");
        return execute(tasks, onTick, 50, title);
    }

    public static <T extends Task<?>> Collection<T> execute(Collection<T> tasks, Renderer onTick, String title) {
        return execute(tasks, onTick, 50, title);
    }

    public static <T extends Task<?>> Collection<T> execute(Collection<T> tasks, Renderer onTick, int tickMs, String title) {
        prepare(tasks, null, tickMs);
        BitRateSmoother smoother = new BitRateSmoother(20);
        long bytesTotal = tasks.stream().mapToLong(Task::getCompletion).sum();
        long tasksTotal = tasks.size();
        List<CompletableFuture<?>> futures = tasks.stream()
                .map(Task::execute)
                .collect(Collectors.toList());
        TimerTask render = Util.timerTask(()->{
            long tasksCompleted = futures.stream().filter(CompletableFuture::isDone).count();
            long bytesCompleted = tasks.stream().mapToLong(Task::getProgress).sum();
            long bitRate = smoother.getAveraged(tasks.stream().mapToLong(Task::pollProgress).sum());
            if (onTick != null) onTick.apply(title, tasksCompleted, tasksTotal, bytesCompleted, bytesTotal, bitRate);
        });
        scheduler.scheduleAtFixedRate(render, 0, tickMs);
        while (!futures.stream().allMatch(CompletableFuture::isDone)) Util.spin(500);
        render.cancel();
        Util.spin(tickMs + 5);
        return tasks;
    }
}
