package net.dirtcraft.dirtlauncher.lib.util;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class Archive extends ZipFile {
    private final File file;
    public Archive(File file) throws ZipException, IOException {
        super(file);
        this.file = file;
    }

    public File asFile() {
        return file;
    }
}
