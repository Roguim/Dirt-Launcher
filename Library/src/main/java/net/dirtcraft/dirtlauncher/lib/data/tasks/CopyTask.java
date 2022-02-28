package net.dirtcraft.dirtlauncher.lib.data.tasks;

import net.dirtcraft.dirtlauncher.lib.config.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public class CopyTask extends FileTask {
    public final File src;

    public CopyTask(File destination, File src) {
        this(destination, src, -0, null);
    }

    public CopyTask(File destination, File src, int size) {
        this(destination, src, size, null);
    }

    public CopyTask(File destination, File src, String sha1) {
        this(destination, src, -0, sha1);
    }

    public CopyTask(File destination, File src, long size, String sha1) {
        super(destination, size, sha1);
        this.src = src;
    }

    public File getSource() {
        return destination;
    }

    @Override
    public InputStream openSource() throws FileNotFoundException {
        return new FileInputStream(destination);
    }

    @Override
    public CompletableFuture<?> prepare() {
        return Constants.COMPLETED_FUTURE;
    }

    @Override
    public String getType() {
        return "Copying";
    }
}
