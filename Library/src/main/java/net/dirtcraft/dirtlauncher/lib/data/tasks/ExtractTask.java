package net.dirtcraft.dirtlauncher.lib.data.tasks;

import net.dirtcraft.dirtlauncher.lib.config.Constants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ExtractTask extends FileTask {
    public final ZipFile arch;
    public final ZipEntry src;

    public static Collection<ExtractTask> from(ZipFile zip, Path folder) {
        Collection<ExtractTask> tasks = new ArrayList<>();
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.isDirectory()) continue;
            File dest = folder.resolve(entry.getName()).toFile();
            long size = entry.getSize();
            tasks.add(new ExtractTask(zip, entry, dest, size));
        }
        return tasks;
    }

    public ExtractTask(ZipFile arch, ZipEntry src, File destination, long size) {
        super(destination, size);
        this.arch = arch;
        this.src = src;
    }

    @Override
    public InputStream openSource() throws IOException {
        return arch.getInputStream(src);
    }

    @Override
    public CompletableFuture<?> preExecute() {
        return Constants.COMPLETED_FUTURE;
    }
}
