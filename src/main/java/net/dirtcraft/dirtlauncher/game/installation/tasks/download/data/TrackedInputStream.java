package net.dirtcraft.dirtlauncher.game.installation.tasks.download.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

public class TrackedInputStream extends InputStream {

    private final InputStream in;
    private final AtomicLong progress;

    public TrackedInputStream(InputStream in, AtomicLong progress){
        this.in = in;
        this.progress = progress;
    }

    @Override
    public int read() throws IOException {
        return in.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException{
        int progress = super.read(b, off, len);
        this.progress.getAndAdd(+progress);
        return progress;
    }

    @Override
    public long skip(long n) throws IOException {
        long progress = super.skip(n);
        this.progress.getAndAdd(+progress);
        return progress;
    }
}
