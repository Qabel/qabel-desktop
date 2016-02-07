package de.qabel.desktop.daemon.management;

import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class MonitoredTransferManagerTest {
	private List<Transaction> transactions = new LinkedList<>();
	private TransferManagerStub wrappedManager = new TransferManagerStub();
	private MonitoredTransferManager manager = new MonitoredTransferManager(wrappedManager);

	@Before
	public void setUp() {
		manager.onAdd((t) -> transactions.add(t));
	}

	@Test
	public void notifiesOnAddUpload() {
		Upload t = new UploadStub();
		manager.addUpload(t);
		assertSame(t, transactions.get(0));
	}

	@Test
	public void notifiesOnAddDownload() {
		Download t = new DownloadStub();
		manager.addDownload(t);
		assertSame(t, transactions.get(0));
	}

	@Test
	public void forwardsCalls() {
		Upload upload = new UploadStub();
		wrappedManager.addUpload(upload);

		assertEquals(1, manager.getTransactions().size());
		assertEquals(1, manager.getHistory().size());
	}

	@Test
	public void addsToWrappedManger() {
		Upload u = new UploadStub();
		manager.addUpload(u);
		Download d = new DownloadStub();
		manager.addDownload(d);

		assertEquals(2, wrappedManager.getTransactions().size());
		assertEquals(2, wrappedManager.getHistory().size());
	}

	@Test
	public void runs() {
		manager.run();
		assertTrue(wrappedManager.hasRun);
	}
}
