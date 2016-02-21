package de.qabel.desktop.daemon.share;

import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;

import java.util.Observable;
import java.util.Observer;

public class ShareNotificationHandler implements Observer {
	private ClientConfiguration clientConfiguration;

	public ShareNotificationHandler(ClientConfiguration clientConfiguration) {
		this.clientConfiguration = clientConfiguration;
	}

	@Override
	public void update(Observable o, Object arg) {
		if (!(arg instanceof PersistenceDropMessage)) {
			return;
		}
		PersistenceDropMessage internalDropMessage = (PersistenceDropMessage)arg;
		DropMessage dropMessage = internalDropMessage.getDropMessage();

		if (!isShareNotification(dropMessage)) {
			return;
		}
		if (internalDropMessage.getSend() || !(internalDropMessage.getReceiver() instanceof Identity)) {
			return;
		}

		ShareNotificationMessage share = ShareNotificationMessage.fromJson(dropMessage.getDropPayload());
		Identity identity = (Identity) internalDropMessage.getReceiver();

		clientConfiguration.getShareNotification(identity).add(share);
	}

	private boolean isShareNotification(DropMessage dropMessage) {
		return dropMessage.getDropPayloadType().equals(DropMessageRepository.PAYLOAD_TYPE_SHARE_NOTIFICATION);
	}
}
