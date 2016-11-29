package de.qabel.desktop.daemon.management;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

public class TransactionGroup extends Observable implements Observer, HasProgressCollection<TransactionGroup, Transaction> {
    protected final Set<Transaction<Path, Path>> transactions = new HashSet<>();
    protected final Map<Transaction, Long> transactionSize = new WeakHashMap<>();
    protected final Map<Transaction, Long> transactionTransferred = new WeakHashMap<>();
    private HashManager hashManager = new HashManager();
    List<Transaction<Path, Path>> transactionsList;

    private long size;
    private long transferred;
    private Runnable progressListener;

    public boolean isEmpty() {
        return transactions.isEmpty();
    }

    public synchronized void add(Transaction transaction) {
        transactions.add(transaction);
        long size = transaction.getSize();
        long transferred = transaction.getTransferred();
        this.size += size;
        this.transferred += transferred;
        transactionSize.put(transaction, size);
        transactionTransferred.put(transaction, transferred);
        progressListener = () -> {
            updateMetadata(transaction);
            notify(transaction);
        };
        transaction.onProgress(progressListener);
        setChanged();
        notify(transaction);
    }

    private synchronized void updateMetadata(Transaction transaction) {
        if (!transactions.contains(transaction)) {
            return;
        }
        long newSize = transaction.getSize();
        long newTransferred = transaction.getTransferred();
        size += newSize - getSize(transaction);
        transferred += newTransferred - getTransferred(transaction);
        transactionSize.put(transaction, newSize);
        transactionTransferred.put(transaction, newTransferred);
    }

    private long getSize(Transaction transaction) {
        return transactionSize.containsKey(transaction) ? transactionSize.get(transaction) : 0;
    }

    private long getTransferred(Transaction transaction) {
        return transactionTransferred.containsKey(transaction) ? transactionTransferred.get(transaction) : 0;
    }

    public int size() {
        return transactions.size();
    }

    public synchronized void clear() {
        transactions.clear();
        transactionSize.clear();
        transactionTransferred.clear();
        size = 0;
        transferred = 0;
        setChanged();
        notifyObservers();
    }

    @Override
    public double getProgress() {
        return size == 0 ? 1.0 : (double) transferred / (double) size;
    }

    @Override
    public long totalSize() {
        return size;
    }

    @Override
    public long currentSize() {
        return transferred;
    }

    private void visitAll(Consumer<Transaction> visitor) {
        synchronized (transactions) {
            for (Transaction t : transactions.toArray(new Transaction[0])) {
                visitor.accept(t);
            }
        }
    }

    @Override
    public TransactionGroup onProgress(Runnable runnable) {
        addObserver((o, arg) -> runnable.run());
        return this;
    }

    @Override
    public void update(Observable o, Object arg) {
        setChanged();
        notifyObservers(o);
    }

    private void notify(Transaction transaction) {
        setChanged();
        notifyObservers(transaction);
    }

    public synchronized void cancel() {
        for (Transaction t : transactions.toArray(new Transaction[0])) {
            if (t.isDone()) {
                continue;
            }
            t.toState(Transaction.STATE.FAILED);
        }
    }

    @Override
    public TransactionGroup onProgress(Consumer<Transaction> consumer) {
        addObserver((o, arg) -> {
            if (arg == null || arg instanceof Transaction) {
                consumer.accept((Transaction) arg);
            }
        });
        return this;
    }

    @Override
    public long totalElements() {
        return transactions.size();
    }

    @Override
    public long finishedElements() {
        final long[] finished = {0};
        visitAll(t -> {
            if (t.isDone()) {
                finished[0]++;
            }
        });

        return finished[0];
    }

    private void loadCurrentTransactions() {
        synchronized (transactions) {
            transactionsList = new ArrayList<>(transactions);
        }
    }

    @Override
    public int totalFiles() {
        loadCurrentTransactions();
        return hashManager.totalFiles(transactionsList);
    }

    @Override
    public int currentFinishedFiles() {
        loadCurrentTransactions();
        return hashManager.finishedFiles(transactionsList);
    }
}
