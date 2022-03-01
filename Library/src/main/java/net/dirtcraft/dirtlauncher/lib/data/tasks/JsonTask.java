package net.dirtcraft.dirtlauncher.lib.data.tasks;

import net.dirtcraft.dirtlauncher.lib.config.Constants;
import net.dirtcraft.dirtlauncher.lib.parsing.JsonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JsonTask<T> extends Task<T> {
    private T result = null;
    private StreamSupplier src;
    private Class<T> type;

    public JsonTask(URL src, Class<T> clazz) {
        this.src = src::openStream;
        this.type = clazz;
    }

    public JsonTask(ZipFile src, ZipEntry file, Class<T> clazz) {
        this.src = ()->src.getInputStream(file);
        this.type = clazz;
    }

    public JsonTask(ZipFile src, String file, Class<T> clazz) {
        this.src = ()->src.getInputStream(src.getEntry(file));
        this.type = clazz;
    }

    @Override
    public InputStream openSource() throws IOException {
        return src.streamUnchecked();
    }

    @Override
    public OutputStream openDestination() throws IOException {
        return null;
    }

    @Override
    public CompletableFuture<?> prepare() {
        return Constants.COMPLETED_FUTURE;
    }

    @Override
    public boolean isComplete() {
        return result != null;
    }

    @Override
    public String getType() {
        return "Fetching";
    }

    @Override
    public T getResult() {
        return result;
    }

    @Override
    public T run() throws ExecutionException, InterruptedException {
        if (result == null) this.execute().get();
        return result;
    }

    @Override
    protected Optional<IOException> complete() {
        try (InputStream in = openSource()) {
            result = JsonUtils.parseJson(in, type);
            return Optional.empty();
        } catch (IOException e) {
            return Optional.of(e);
        }
    }

    public interface StreamSupplier {
        InputStream stream() throws Exception;

        default InputStream streamUnchecked(){
            try {
                return stream();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private static class TrackedInputStream extends InputStream {
        private final InputStream wrapped;

        private TrackedInputStream(InputStream wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public int read() throws IOException {
            return wrapped.read();
        }
    }
}
