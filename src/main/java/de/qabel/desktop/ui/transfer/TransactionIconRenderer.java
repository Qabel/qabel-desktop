package de.qabel.desktop.ui.transfer;

import de.qabel.desktop.daemon.management.Transaction;
import de.qabel.desktop.daemon.management.Upload;
import javafx.scene.image.Image;

import java.util.function.Function;

public class TransactionIconRenderer implements Function<Transaction, Image> {
	private static final Image folderDownloadImg = getImage("/icon/add_folder.png");
	private static final Image folderUploadImg = getImage("/icon/folder-upload.png");
	private static final Image fileDownloadImg = getImage("/icon/download.png");
	private static final Image fileUploadImg = getImage("/icon/upload.png");


	private static Image getImage(String resourceName) {
		return new Image(TransactionIconRenderer.class.getResourceAsStream(resourceName), 18, 18, true, true);
	}

	@Override
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
				return fileDownloadImg;
			}
		}
	}
}
