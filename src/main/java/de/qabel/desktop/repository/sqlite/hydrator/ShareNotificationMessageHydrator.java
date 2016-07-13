package de.qabel.desktop.repository.sqlite.hydrator;

import de.qabel.core.repository.EntityManager;
import de.qabel.core.repository.sqlite.hydrator.AbstractHydrator;
import de.qabel.desktop.daemon.drop.ShareNotificationMessage;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ShareNotificationMessageHydrator extends AbstractHydrator<ShareNotificationMessage> {
    private EntityManager em;

    public ShareNotificationMessageHydrator(EntityManager em) {
        this.em = em;
    }

    @Override
    protected String[] getFields() {
        return new String[]{"id", "url", "key", "message"};
    }

    @Override
    public ShareNotificationMessage hydrateOne(ResultSet resultSet) throws SQLException {
        int i = 1;
        int id = resultSet.getInt(i++);
        if (em.contains(ShareNotificationMessage.class, id)) {
            return em.get(ShareNotificationMessage.class, id);
        }

        String url = resultSet.getString(i++);
        String key = resultSet.getString(i++);
        String message = resultSet.getString(i++);
        ShareNotificationMessage share = new ShareNotificationMessage(url, key, message);
        share.setId(id);
        recognize(share);
        return share;
    }

    @Override
    public void recognize(ShareNotificationMessage instance) {
        em.put(ShareNotificationMessage.class, instance);
    }
}
