package de.qabel.desktop.ui.transfer;

import de.qabel.desktop.daemon.management.Transaction;
import de.qabel.desktop.daemon.management.Upload;
import javafx.scene.image.Image;

import java.util.function.Function;

public class TransactionIconRenderer implements Function<Transaction, Image> {
	private static final Image folderDownloadImg = new Image(TransactionIconRenderer.class.getResourceAsStream("/icon/add_folder.png"), 18, 18, true, true);
	private static final Image folderUploadImg = new Image(TransactionIconRenderer.class.getResourceAsStream("/icon/folder-upload.png"), 18, 18, true, true);
	private static final Image fileDownloadImg = new Image(TransactionIconRenderer.class.getResourceAsStream("/icon/download.png"), 18, 18, true, true);
	private static final Image fileUploadImg = new Image(TransactionIconRenderer.class.getResourceAsStream("/icon/upload.png"), 18, 18, true, true);

	public Image apply(Transaction transaction) {
		if (transaction.isDir()) {
			if (transaction instanceof Upload) {
				return folderUploadImg;
			} else {
				return folderDownloadImg;
			}
		} else {
			if (transaction instanceof Upload) {
				return fileUploadImg;
			} else {
				return folderUploadImg;
			}
		}
	}
}
