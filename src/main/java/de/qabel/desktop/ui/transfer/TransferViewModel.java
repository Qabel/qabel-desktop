package de.qabel.desktop.ui.transfer;

import de.qabel.desktop.daemon.management.HasProgressCollection;
import de.qabel.desktop.daemon.management.Transaction;
import de.qabel.desktop.daemon.management.Upload;
import javafx.beans.property.*;
import javafx.scene.image.Image;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Function;

public class TransferViewModel extends TransactionFxProgressCollectionModel {
	private Function<Transaction, String> transactionLabelRenderer = t -> t == null ? "" : render(t);
	private Function<Transaction, Image> transactionImageRenderer = new TransactionIconRenderer();
	private StringProperty currentTransactionLabel = new SimpleStringProperty("");
	private LongProperty currentTransactionPercent = new SimpleLongProperty();
	private StringProperty currentTransactionPercentLabel = new SimpleStringProperty("");
	private ObjectProperty<Image> currentTransactionImage = new SimpleObjectProperty<>();
	private BooleanProperty currentTransactionImageVisible = new SimpleBooleanProperty(false);

	public TransferViewModel(HasProgressCollection<?, Transaction> progress) {
		super(progress);

		currentItemProperty().addListener((observable, oldValue, newValue) -> {
			currentTransactionLabel.setValue(transactionLabelRenderer.apply(newValue));
			currentTransactionImage.set(newValue == null ? null : transactionImageRenderer.apply(newValue));
		});
		onChange(this::update);
	}

	private void update(Transaction transaction) {
		if (transaction == null || transaction.getState() != Transaction.STATE.RUNNING) {
			currentTransactionPercent.setValue(0);
			currentTransactionPercentLabel.setValue("");
			currentTransactionImageVisible.setValue(false);
			return;
		}
		int progressPercent = (int) (transaction.getProgress() * 100);
		currentTransactionPercent.setValue(progressPercent);
		currentTransactionPercentLabel.setValue(progressPercent + " %");
		currentTransactionImageVisible.setValue(true);
	}

	public String render(Transaction transaction) {
		String filename = transaction.getDestination().toString();
		if (transaction.getDestination().getFileName() != null) {
			filename = transaction.getDestination().getFileName().toString();
		}
		String direction = transaction instanceof Upload ? "Remote" : "Local";
		String type = transaction.getType().toString();
		return filename + " (" + StringUtils.capitalize(type) + " " + transaction.getSize() / 1024 + "kb)";
	}

	public StringProperty currentTransactionLabel() {
		return currentTransactionLabel;
	}

	public LongProperty currentTransactionPercent() {
		return currentTransactionPercent;
	}

	public StringProperty currentTransactionPercentLabel() {
		return currentTransactionPercentLabel;
	}

	public BooleanProperty currentTransactionImageVisible() {
		return currentTransactionImageVisible;
	}

	public ObjectProperty<Image> currentTransactionImage() {
		return currentTransactionImage;
	}

	public void setTransactionLabelRenderer(Function<Transaction, String> transactionLabelRenderer) {
		this.transactionLabelRenderer = transactionLabelRenderer;
	}
}
