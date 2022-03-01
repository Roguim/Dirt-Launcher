package net.dirtcraft.dirtlauncher.lib.util;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

public class Jar extends JarFile {
    private final File file;
    public Jar(File file) throws IOException {
        super(file);
        this.file = file;
    }

    public File asFile() {
        return file;
    }
}
