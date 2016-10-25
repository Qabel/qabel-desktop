package de.qabel.desktop.daemon.share;

import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.ShareNotificationRepository;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Observable;
import java.util.Observer;

public class ShareNotificationHandler implements Observer {
    private static final Logger logger = LoggerFactory.getLogger(ShareNotificationHandler.class);
    private ShareNotificationRepository shareNotificationRepo;

    public ShareNotificationHandler(ShareNotificationRepository shareNotificationRepo) {
        this.shareNotificationRepo = shareNotificationRepo;
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
        if (internalDropMessage.isSent() || !(internalDropMessage.getReceiver() instanceof Identity)) {
            return;
        }

        ShareNotificationMessage share = ShareNotificationMessage.fromJson(dropMessage.getDropPayload());
        Identity identity = (Identity) internalDropMessage.getReceiver();

        try {
            shareNotificationRepo.save(identity, share);
        } catch (PersistenceException e) {
            logger.error("failed to save share: " + e.getMessage(), e);
        }
    }

    private boolean isShareNotification(DropMessage dropMessage) {
        return dropMessage.getDropPayloadType().equals(DropMessageRepository.PAYLOAD_TYPE_SHARE_NOTIFICATION);
    }
}
