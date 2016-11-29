package de.qabel.desktop.daemon.management;

import de.qabel.desktop.nio.boxfs.BoxFileSystem;
import de.qabel.desktop.nio.boxfs.BoxPath;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import static de.qabel.desktop.daemon.management.Transaction.STATE.FINISHED;
import static de.qabel.desktop.daemon.management.Transaction.STATE.SKIPPED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class WindowedTransactionGroupTest extends TransactionGroupTest {

    private UploadStub t1 = new UploadStub();
    private UploadStub t2 = new UploadStub();
    private UploadStub t3 = new UploadStub();

    private final WindowedTransactionGroup group = new WindowedTransactionGroup();
    private BoxPath syncRoot = BoxFileSystem.getRoot().resolve("syncRoot");
    protected Path tmpDir;

    @Before
    public void setUp() {
        try {
            tmpDir = Files.createTempDirectory(getClass().getSimpleName());
        } catch (Exception e) {
            fail("failed to create tmp dir: " + e.getMessage());
        }
    }

    private Path tmpPath(String dir) {
        return Paths.get(tmpDir.toString(), dir);
    }

    @Test
    public void testClosesWindowWhenAllTransactionsAreFinished() {
        t1.setSize(100);
        t1.setTransferred(100);

        WindowedTransactionGroup group = new WindowedTransactionGroup();
        group.add(t1);
        t1.toState(FINISHED);

        UploadStub t2 = new UploadStub();
        t2.setSize(100);
        t2.setTransferred(0);
        group.add(t2);

        assertEquals(0.0, group.getProgress(), 0.001);
    }

    @Test
    public void givenUnfinishedProgress_allTransactionsRemainInWindow() {
        t1.setSize(100);
        t1.setTransferred(0);

        t2.setSize(100);
        t2.setTransferred(0);

        group.add(t1);
        group.add(t2);

        t1.setTransferred(100);
        t1.toState(FINISHED);

        assertEquals(0.5, group.getProgress(), 0.001);
    }

    @Override
    @Test
    public void notifiesOnProgressChangeViaAdd() {
        List<Object> updates = new LinkedList<>();
        TransactionStub upload1 = new TransactionStub();
        WindowedTransactionGroup group = new WindowedTransactionGroup();
        upload1.setSize(100);
        group.addObserver((o, arg) -> updates.add(arg));

        group.add(upload1);
        assertEquals(1, updates.size());
    }

    @Test
    public void doesntAddskippedTransactions() {
        t1.setSize(100);
        t1.setTransferred(0);
        t1.toState(SKIPPED);

        group.add(t1);
        assertEquals(1.0, group.getProgress(), 0.001);
    }

    public void createTransaction(UploadStub transaction, String sourcePath) {
        transaction.setSize(1024);
        transaction.isDir = false;
        transaction.source = tmpPath(sourcePath);
        transaction.destination = syncRoot;
    }

    @Test
    public void countDifferentFiles() {
        createTransaction(t1, "test1");
        createTransaction(t2, "test2");
        createTransaction(t3, "test3");

        group.add(t1);
        group.add(t2);
        group.add(t3);

        assertEquals(3, group.totalFiles());
    }

    @Test
    public void countEqualFiles() {
        createTransaction(t1, "test1");
        createTransaction(t2, "test1");
        createTransaction(t3, "test1");

        group.add(t1);
        group.add(t2);
        group.add(t3);
        assertEquals(1, group.totalFiles());
    }

    @Test
    public void countFinishedFiles() {
        createTransaction(t1, "test1");
        createTransaction(t2, "test2");
        createTransaction(t3, "test3");

        group.add(t1);
        group.add(t2);
        group.add(t3);

        assertEquals(3, group.totalFiles());

        t1.setTransferred(100);
        t1.toState(FINISHED);

        t2.setTransferred(100);
        t2.toState(SKIPPED);

        assertEquals(2, group.currentFinishedFiles());
    }
}
