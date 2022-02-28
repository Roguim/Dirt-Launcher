package net.dirtcraft.dirtlauncher.lib.data.tasks;

import net.dirtcraft.dirtlauncher.lib.config.Constants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ExtractTask extends FileTask {
    public final ZipFile arch;
    public final ZipEntry src;

    public static Collection<ExtractTask> from(File zip, Path folder) {
        try {
            return from(new ZipFile(zip), folder);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.EMPTY_LIST;
        }
    }

    public static Collection<ExtractTask> from(ZipFile zip, Path folder) {
        Collection<ExtractTask> tasks = new ArrayList<>();
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.isDirectory()) continue;
            File dest = folder.resolve(entry.getName()).toFile();
            tasks.add(new ExtractTask(zip, entry, dest));
        }
        return tasks;
    }

    public ExtractTask(ZipFile arch, String src, File destination) {
        super(destination);
        this.src = arch.getEntry(src);
        this.completion = this.src.getSize();
        this.arch = arch;

    }

    public ExtractTask(ZipFile arch, ZipEntry src, File destination) {
        super(destination);
        this.src =src;
        this.completion = this.src.getSize();
        this.arch = arch;

    }

    @Override
    public InputStream openSource() throws IOException {
        return arch.getInputStream(src);
    }

    @Override
    public CompletableFuture<?> prepare() {
        return Constants.COMPLETED_FUTURE;
    }

    @Override
    public String getType() {
        return "Extracting";
    }
}
