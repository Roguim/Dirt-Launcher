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
    private static final long tickMs = 200;
    private static final Timer scheduler = new Timer();
    public static  <T extends Task<?>> Collector<T, List<T>, List<T>> collector(Renderer onTick, String title){
        return new ExecuteCollector<>(onTick, title);
    }

    public static Collection<? extends Task<?>> prepare(Collection<? extends Task<?>> tasks, Renderer.ProgressRenderer onTick) {
        return prepare(tasks, onTick, "Preparing");
    }

    public static Collection<? extends Task<?>> prepare(Collection<? extends Task<?>> tasks, Renderer.ProgressRenderer onTick, String title) {
        List<CompletableFuture<?>> futures = tasks.stream()
                .map(Task::prepare)
                .collect(Collectors.toList());
        long tasksTotal = tasks.size();
        TimerTask render = Util.timerTask(()->{
            long tasksCompleted = futures.stream().filter(CompletableFuture::isDone).count();
            if (onTick != null) onTick.apply(title, tasksCompleted, tasksTotal, 0, 0, 0);
        });
        scheduler.scheduleAtFixedRate(render, 0, tickMs);
        while (!futures.stream().allMatch(CompletableFuture::isDone)) Util.spin(500);
        render.cancel();
        Util.spin(tickMs + 5);
        return tasks;
    }

    public static <T extends Task<?>> Collection<T> execute(Collection<T> tasks, Renderer onTick) {
        String title = tasks.stream().findFirst().map(Task::getType).orElse("");
        return execute(tasks, onTick, title);
    }

    public static <T extends Task<?>> Collection<T> execute(Collection<T> tasks, Renderer onTick, String title) {
        prepare(tasks, null);
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

    public static <T extends Task<?>> T execute(T task, Renderer onTick, String title) {
        try {
            task.prepare().wait(); //prepare(tasks, null);
        } catch (Exception ignored) {}
        BitRateSmoother smoother = new BitRateSmoother(20);
        CompletableFuture<?> future = task.execute();
        TimerTask render = Util.timerTask(()->{
            if (onTick != null) onTick.apply(title, 0, 1, task.getProgress(), task.completion, smoother.getAveraged(task.pollProgress()));
        });
        scheduler.scheduleAtFixedRate(render, 0, tickMs);
        while (!future.isDone()) Util.spin(500);
        render.cancel();
        Util.spin(tickMs + 5);
        return task;
    }

    private static class ExecuteCollector<T extends Task<?>> implements Collector<T, List<T>, List<T>>  {
        private final EnumSet<Characteristics> CHARS = EnumSet.of(Characteristics.UNORDERED);
        private final Renderer onTick;
        private final String title;

        private ExecuteCollector(Renderer onTick, String title) {
            this.onTick = onTick;
            this.title = title;
        }

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
            return CHARS;
        }
    }
}
