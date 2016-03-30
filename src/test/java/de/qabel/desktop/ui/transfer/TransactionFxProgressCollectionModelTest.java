package de.qabel.desktop.ui.transfer;

import de.qabel.desktop.AsyncUtils;
import de.qabel.desktop.daemon.management.Transaction;
import de.qabel.desktop.daemon.management.TransactionStub;
import de.qabel.desktop.daemon.management.WindowedTransactionGroup;
import de.qabel.desktop.ui.AbstractControllerTest;
import org.junit.Before;
import org.junit.Test;

import static de.qabel.desktop.daemon.management.Transaction.STATE.FAILED;
import static de.qabel.desktop.daemon.management.Transaction.STATE.RUNNING;

public class TransactionFxProgressCollectionModelTest extends AbstractControllerTest {

	private final WindowedTransactionGroup progress = new WindowedTransactionGroup();
	private final TransactionFxProgressCollectionModel model = new TransactionFxProgressCollectionModel(progress);
	private final TransactionStub t = new TransactionStub();

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void testKnowsWhenRunning() throws Exception {
		t.setSize(100);
		progress.add(t);
		t.toState(RUNNING);

		AsyncUtils.waitUntil(model.runningProperty()::get);
	}

	@Test
	public void testKnowsWhenFinished() throws  Exception {
		t.setSize(100);
		progress.add(t);
		t.toState(RUNNING);
		AsyncUtils.waitUntil(model.runningProperty()::get);
		t.toState(FAILED);

		AsyncUtils.waitUntil(model.runningProperty().not()::get, 1000L, () -> "expected not running but is " +  (model.runningProperty().get() ? "running" : "not running"));
	}
}
