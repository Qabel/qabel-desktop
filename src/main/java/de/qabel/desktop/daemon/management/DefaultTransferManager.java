package de.qabel.desktop.daemon.management;


import de.qabel.desktop.daemon.management.exception.TransferSkippedException;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static de.qabel.desktop.daemon.management.Transaction.STATE.*;
import static de.qabel.desktop.daemon.management.Transaction.TYPE.CREATE;
import static de.qabel.desktop.daemon.management.Transaction.TYPE.DELETE;
import static de.qabel.desktop.daemon.management.Transaction.TYPE.UPDATE;

public class DefaultTransferManager extends Observable implements TransferManager {
    private final Logger logger = LoggerFactory.getLogger(DefaultTransferManager.class);
    private final LinkedBlockingQueue<Transaction> transactions = new LinkedBlockingQueue<>();
    private final List<Transaction> history = Collections.synchronizedList(new LinkedList<>());

    @Override
    public List<Transaction> getTransactions() {
        return new LinkedList<>(transactions);
    }

    @Override
    public void addDownload(Download download) {
        logger.trace("download added: " + download.getSource() + " to " + download.getDestination());
        transactions.add(download);
        history.add(download);
    }

    @Override
    public List<Transaction> getHistory() {
        return Collections.unmodifiableList(history);
    }

    @Override
    public void cleanup() {
        synchronized (transactions) {
            Iterator<Transaction> iter = transactions.iterator();
            Transaction transaction;
            while (iter.hasNext()) {
                transaction = iter.next();
                if (transaction.isDone() || !transaction.isValid()) {
                    iter.remove();
                }
            }
        }
    }

    @Override
    public void addUpload(Upload upload) {
        logger.trace("upload added: " + upload.getSource() + " to " + upload.getDestination());
        transactions.add(upload);
        history.add(upload);
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                next();
            }
        } catch (InterruptedException e) {
            logger.debug("loadManager stopped: " + e.getMessage());
        }
    }

    public void next() throws InterruptedException {
        Transaction transaction = transactions.take();
        transaction.toState(WAITING);
        setChanged();
        notifyObservers(transaction);
        if (isStageable(transaction)) {
            long delay = transaction.getStagingDelayMillis() - transaction.transactionAge();
            if (delay > 0) {
                Thread.sleep(delay);
            }
        }
        logger.trace("handling transaction  " + transaction);
        try {
            transaction.toState(RUNNING);
            if (transaction instanceof Upload) {
                upload((Upload) transaction);
            } else {
                download((Download) transaction);
            }
        } catch (Exception e) {
            logger.error("Transaction failed: " + e.getMessage(), e);
        } finally {
            setChanged();
            notifyObservers(null);
        }
    }

    private boolean isStageable(Transaction transaction) {
        return transaction instanceof Upload;
    }

    void download(Download download) throws Exception {
        try (Download ignored = download) {
            if (!download.isValid()) {
                throw new TransferSkippedException("download says it's invalid");
            }

            Path destination = download.getDestination();
            Path source = download.getSource();

            switch (download.getType()) {
                case UPDATE:
                case CREATE:
                    if (download.isDir()) {
                        Files.createDirectories(destination);
                        download.setMtime(Files.getLastModifiedTime(destination).toMillis());
                        break;
                    }

                    Path parent = source.getParent();
                    BoxNavigation nav = navigate(parent, download.getBoxVolume());
                    BoxFile file = nav.getFile(source.getFileName().toString());
                    download.setSize(file.getSize());
                    if (remoteChanged(download, file)) {
                        throw new TransferSkippedException("remote has changed");
                    }
                    if (localIsNewer(destination, file)) {
                        throw new TransferSkippedException("local is newer");
                    }

                    ProgressListener listener = new TransactionRelatedProgressListener(download);
                    try (InputStream stream = new BufferedInputStream(nav.download(file, listener))) {
                        Path tmpFile = createTempFileForDownload(destination);
                        try {
                            Files.copy(stream, tmpFile, StandardCopyOption.REPLACE_EXISTING);
                            Files.setLastModifiedTime(tmpFile, FileTime.fromMillis(download.getMtime()));
                            Files.move(tmpFile, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                            if (Files.getLastModifiedTime(destination).toMillis() != download.getMtime()) {
                                Files.setLastModifiedTime(destination, FileTime.fromMillis(download.getMtime()));
                            }
                        } finally {
                            Files.deleteIfExists(tmpFile);
                        }
                    }
                    break;
                case DELETE:
                    if (download.isDir()) {
                        FileUtils.deleteDirectory(destination.toFile());
                    } else {
                        Files.deleteIfExists(destination);
                    }
                    break;
            }
            download.toState(FINISHED);
        } catch (TransferSkippedException e) {
            download.toState(SKIPPED);
            logger.debug("skipped download "  + " (" + e.getMessage() + ")");
        } catch (Exception e) {
            download.toState(FAILED);
            throw e;
        }
    }

    private Path createTempFileForDownload(Path destinationFile) throws IOException {
        String filename = destinationFile.getFileName().toString();
        Path tmpFile = destinationFile.getParent().resolve("." + filename + ".qpart~");
        return tmpFile;
    }

    private boolean localIsNewer(Path local, BoxFile file) {
        try {
            return file.getMtime() < Files.getLastModifiedTime(local).toMillis();
        } catch (IOException e) {
            return false;
        }
    }

    private boolean remoteChanged(Download download, BoxFile file) {
        return !Objects.equals(file.getMtime(), download.getMtime());
    }

    void upload(Upload upload) throws QblStorageException {
        try (Upload ignored = upload) {
            if (!upload.isValid()) {
                throw new TransferSkippedException("upload says it's invalid");
            }

            executeUpload(upload);
            upload.toState(FINISHED);
            logger.trace("finished upload " + upload);
        } catch (TransferSkippedException e) {
            upload.toState(SKIPPED);
            logger.trace("skipped upload " + upload + " (" + e.getMessage() + ")");
        } catch (Exception e) {
            upload.toState(FAILED);
            throw e;
        }
    }

    private void executeUpload(Upload upload) throws QblStorageException, TransferSkippedException {
        Path destination = upload.getDestination();
        Path source = upload.getSource();

        BoxVolume volume = upload.getBoxVolume();
        Path parent = destination.getParent();
        BoxNavigation dir;

        SimpleDateFormat format = new SimpleDateFormat("D.M.Y H:m:s");

        if (upload.isDir() && (upload.getType() == UPDATE || upload.getType() == CREATE)) {
            createDirectory(destination, volume);
            return;
        }

        String filename = destination.getFileName().toString();
        if (upload.getType() == DELETE) {
            dir = navigate(parent, volume);
            if (dir.hasFolder(filename)) {
                dir.delete(dir.getFolder(filename));
            } else {
                BoxFile file = dir.getFile(filename);
                if (remoteIsNewer(upload, file)) {
                    throw new TransferSkippedException("remote is newer " + format.format(new Date(file.getMtime())) + ">" + format.format(new Date(upload.getMtime())));
                }
                dir.delete(file);
            }
            return;
        }

        ProgressListener listener = new TransactionRelatedProgressListener(upload);

        dir = createDirectory(parent, volume);
        if (!dir.hasFile(filename)) {
            uploadFile(dir, source, destination, listener);
            return;
        }

        BoxFile file = dir.getFile(filename);
        if (remoteIsNewer(upload, file)) {
            throw new TransferSkippedException("remote is newer " + format.format(new Date(file.getMtime())) + ">" + format.format(new Date(upload.getMtime())));
        }

        overwriteFile(dir, source, destination, listener);
    }

    private boolean remoteIsNewer(Upload upload, BoxFile file) {
        return file.getMtime() >= upload.getMtime();
    }

    private BoxNavigation navigate(Path path, BoxVolume volume) throws QblStorageException {
        BoxNavigation nav = volume.navigate();
        for (int i = 0; i < path.getNameCount(); i++) {
            nav = nav.navigate(path.getName(i).toString());
        }
        return nav;
    }

    private void uploadFile(BoxNavigation dir, Path source, Path destination, ProgressListener listener) throws QblStorageException {
        dir.upload(destination.getFileName().toString(), source.toFile(), listener);
    }

    private void overwriteFile(BoxNavigation dir, Path source, Path destination, ProgressListener listener) throws QblStorageException {
        dir.overwrite(destination.getFileName().toString(), source.toFile(), listener);
    }

    private BoxNavigation createDirectory(Path destination, BoxVolume volume) throws QblStorageException {
        BoxNavigation nav = volume.navigate();
        for (int i = 0; i < destination.getNameCount(); i++) {
            String name = destination.getName(i).toString();
            try {
                nav = nav.navigate(name);
            } catch (IllegalArgumentException e) {
                nav = nav.navigate(nav.createFolder(name));
            }
        }
        return nav;
    }
}
