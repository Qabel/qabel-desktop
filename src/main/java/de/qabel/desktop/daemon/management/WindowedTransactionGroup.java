package de.qabel.desktop.daemon.management;

import java.util.HashSet;
import java.util.Set;

import static de.qabel.desktop.daemon.management.Transaction.STATE.FAILED;
import static de.qabel.desktop.daemon.management.Transaction.STATE.FINISHED;
import static de.qabel.desktop.daemon.management.Transaction.STATE.SKIPPED;

public class WindowedTransactionGroup extends TransactionGroup {
    private Set<Transaction> runningTransactions = new HashSet<>();

    @Override
    public void add(Transaction transaction) {
        if (!isDone(transaction)) {
            transaction.onProgress(() -> {
                if (isDone(transaction)) {
                    runningTransactions.remove(transaction);
                }
                if (runningTransactions.isEmpty()) {
                    clear();
                }
            });
            super.add(transaction);
            runningTransactions.add(transaction);
        }
    }

    private boolean isDone(Transaction transaction) {
        return transaction.getState() == FINISHED
                || transaction.getState() == FAILED
                || transaction.getState() == SKIPPED;
    }

    @Override
    public long finishedElements() {
        return totalElements() - runningTransactions.size();
    }
}
