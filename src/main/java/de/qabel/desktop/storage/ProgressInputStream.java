package de.qabel.desktop.storage;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ProgressInputStream extends FilterInputStream {
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private Consumer<Long> consumer;
    private long read;
    private long lastUpdate;

    /**
     * Creates a <code>FilterInputStream</code>
     * by assigning the  argument <code>in</code>
     * to the field <code>this.in</code> so as
     * to remember it for later use.
     *
     * @param in the underlying input stream, or <code>null</code> if
     *           this instance is to be created without an underlying stream.
     */
    protected ProgressInputStream(InputStream in, Consumer<Long> consumer) {
        super(in);
        this.consumer = consumer;
    }

    @Override
    public int read() throws IOException {
        int read = super.read();
        add(1);
        return read;
    }

    private void add(long bytes) {
        if (bytes <= 0) {
            return;
        }
        read += bytes;
        update();
    }

    private void update() {
        update(false);
    }

    private void update(boolean force) {
        long now = System.currentTimeMillis();
        //		if (force || now < lastUpdate + 100) {
        //			return;
        //		}
        lastUpdate = now;
        executor.submit(() -> consumer.accept(read));
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = super.read(b, off, len);
        add(read);
        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        long skip = super.skip(n);
        add(skip);
        return skip;
    }

    @Override
    public void close() throws IOException {
        update(true);
        super.close();
    }

    @Override
    public synchronized void reset() throws IOException {
        super.reset();
    }
}
