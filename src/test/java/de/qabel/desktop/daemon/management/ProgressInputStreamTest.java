package de.qabel.desktop.daemon.management;

import de.qabel.desktop.AsyncUtils;
import de.qabel.desktop.storage.ProgressInputStream;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ProgressInputStreamTest {
    @Test
    public void notifiesAboutReads() throws IOException {
        List<Long> updates = new LinkedList<>();
        ByteArrayInputStream in = new ByteArrayInputStream("+".getBytes());
        ProgressInputStream sut = new ProgressInputStream(in, updates::add);
        try {

            byte[] b = new byte[1];
            sut.read(b);
            assertEquals("+", new String(b));
            AsyncUtils.waitUntil(() -> updates.size() == 1);
            assertEquals(1L, (long) updates.get(0));
        } finally {
            sut.close();
        }
    }

    @Test
    public void notifiesOnClose() throws IOException {
        List<Long> updates = new LinkedList<>();
        ByteArrayInputStream in = new ByteArrayInputStream("+".getBytes());
        ProgressInputStream sut = new ProgressInputStream(in, updates::add);
        try {
            sut.close();
            AsyncUtils.waitUntil(() -> updates.size() == 1);
            assertEquals(0L, (long) updates.get(0));
        } finally {
            sut.close();
        }
    }
}
