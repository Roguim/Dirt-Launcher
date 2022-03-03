package net.dirtcraft.dirtlauncher.lib.data.tasks;

import net.dirtcraft.dirtlauncher.lib.config.Constants;
import net.dirtcraft.dirtlauncher.lib.util.Util;

import java.io.*;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public abstract class FileTask extends Task<File> {
    private boolean completed = false;
    public final File destination;
    protected String sha1;

    public FileTask(File destination) {
        this.destination = destination;
    }

    public FileTask(File destination, long size) {
        this(destination, size, null);
    }

    public FileTask(File destination, String sha1) {
        this(destination, -0, sha1);
    }

    public FileTask(File destination, long size, String sha1) {
        this.destination = destination;
        this.completion = size;
        this.sha1 = sha1;
    }

    public String getSha1() {
        return sha1;
    }

    public File getDestination() {
        return destination;
    }

    @Override
    public OutputStream openDestination() throws FileNotFoundException {
        return new FileOutputStream(destination);
    }

    @Override
    public CompletableFuture<?> execute(){
        if (isComplete()) {
            this.progress.set(completion);
            return Constants.COMPLETED_FUTURE;
        }
        else return super.execute();
    }

    @Override
    public File runOrThrow() throws ExecutionException, InterruptedException {
        if (!completed) {
            this.execute().get();
            completed = true;
        }
        return destination;
    }

    @Override
    protected void tryComplete(){
        //noinspection ResultOfMethodCallIgnored
        destination.getParentFile().mkdirs();
        super.tryComplete();
    }

    @Override
    public File getResult() {
        return destination;
    }

    @Override
    public boolean isComplete() {
        if (sha1 == null || sha1.length() < 30 || !destination.exists()) return false;
        try {
            return sha1.matches("(?i)" + Util.getFileSha1(destination));
        } catch (IOException e) {
            return false;
        }
    }
}
