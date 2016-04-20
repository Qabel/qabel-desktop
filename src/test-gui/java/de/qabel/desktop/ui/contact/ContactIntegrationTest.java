package de.qabel.desktop.ui.contact;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.core.config.Contact;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.drop.DropMessage;
import de.qabel.desktop.daemon.drop.TextMessage;
import de.qabel.desktop.repository.EntityManager;
import de.qabel.desktop.repository.sqlite.*;
import de.qabel.desktop.ui.AbstractGuiTest;
import de.qabel.desktop.ui.actionlog.ActionlogController;
import de.qabel.desktop.ui.actionlog.PersistenceDropMessage;
import de.qabel.desktop.ui.contact.item.ContactItemController;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashSet;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ContactIntegrationTest extends AbstractGuiTest<ContactController> {
    private Connection connection;
    private ClientDatabase clientDatabase;
    private EntityManager em = new EntityManager();
    private final Contact contact = new Contact("contact", new HashSet<>(), new QblECPublicKey("contactKey".getBytes()));

    @Before
    @Override
    public void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA FOREIGN_KEYS = ON");
        }
        clientDatabase = new DesktopClientDatabase(connection);
        clientDatabase.migrate();
        initRepositories();

        super.setUp();

        contactRepository.save(contact, identity);
    }

    private void initRepositories() throws Exception {
        transactionManager = new SqliteTransactionManager(connection);
        identityRepository = new SqliteIdentityRepository(clientDatabase, em);
        contactRepository = new SqliteContactRepository(clientDatabase, em);
        dropMessageRepository = new SqliteDropMessageRepository(clientDatabase, em);
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        connection.close();
    }

    @Override
    protected FXMLView getView() {
        return new ContactView();
    }

    @Override
    protected Object launchNode(FXMLView view) {
        return null;
    }

    private ContactController launchNode() {
        Object presenter = super.launchNode(getView());
        controller = (ContactController) presenter;
        return controller;
    }

    @Test
    public void testShowsNotificationForOldUnseenMessages() throws Exception {
        dropMessageRepository.save(getUnseenMessage());
        dropMessageRepository.save(getSeenMessage());

        launchNode();

        waitUntil(() -> controller.contactItems.size() == 1);
        ContactItemController itemController = controller.contactItems.get(0);

        waitUntil(() -> itemController.getIndicator().getText().equals("1"));
        clickOn("#avatarContainer");
        waitUntil(() -> itemController.getIndicator().getText().equals("0"));

        launchNode();
        waitUntil(() -> controller.contactItems.size() == 1);
    }

    @Test
    public void actionlogReceivesNewMessages() throws Exception {
        launchNode();
        clickOn("#avatarContainer");
        waitUntil(() -> controller.actionlogController != null);
        ActionlogController actionlogController = controller.actionlogController;
        assertThat(actionlogController.getReceivedDropMessages(), is(empty()));

        dropMessageRepository.save(getUnseenMessage());
        assertThat(actionlogController.getReceivedDropMessages(), hasSize(1));
    }

    private PersistenceDropMessage getSeenMessage() {
        DropMessage dropMessage2 = new DropMessage(contact, new TextMessage("message").toJson(), "type");
        return new PersistenceDropMessage(dropMessage2, contact, identity, false, true);
    }

    private PersistenceDropMessage getUnseenMessage() {
        DropMessage dropMessage = new DropMessage(contact, new TextMessage("message").toJson(), "type");
        return new PersistenceDropMessage(dropMessage, contact, identity, false, false);
    }
}
