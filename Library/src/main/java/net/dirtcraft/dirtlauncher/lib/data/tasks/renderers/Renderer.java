package net.dirtcraft.dirtlauncher.lib.data.tasks.renderers;

public interface Renderer {
    void apply(String title, long tasksCompleted, long tasksTotal, long bytesCompleted, long bytesTotal, long bitrate);

    interface BitrateRenderer extends Renderer {
        void apply(String title, long bytesCompleted, long bytesTotal, long bitrate, double percent);

        default void apply(String title, long tasksCompleted, long tasksTotal, long bytesCompleted, long bytesTotal, long bitrate){
            apply(title, bytesCompleted, bytesTotal, bitrate, Math.min((double) bytesCompleted / bytesTotal, 1D));
        }
    }

    interface ProgressRenderer extends Renderer {
        void apply(String title, long tasksCompleted, long tasksTotal, double percent);

        default void apply(String title, long tasksCompleted, long tasksTotal, long bytesCompleted, long bytesTotal, long bitrate){
            apply(title, tasksCompleted, tasksTotal, Math.min((double) bytesCompleted / bytesTotal, 1D));
        }
    }
}
