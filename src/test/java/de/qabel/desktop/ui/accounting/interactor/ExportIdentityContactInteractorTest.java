package de.qabel.desktop.ui.accounting.interactor;

import com.airhacks.afterburner.views.QabelFXMLView;
import com.google.common.io.Files;
import de.qabel.core.config.Contact;
import de.qabel.core.config.ContactExportImport;
import de.qabel.core.config.Identity;
import de.qabel.desktop.ui.AbstractControllerTest;
import de.qabel.desktop.ui.util.CallbackFileChooserFactory;
import org.junit.Before;
import org.junit.Test;
import rx.observers.TestSubscriber;

import java.io.File;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class ExportIdentityContactInteractorTest extends AbstractControllerTest {
    private static File testfile = new File(Files.createTempDir().getAbsolutePath() + "/tmpfile");
    private ExportIdentityContactInteractor interactor;
    private AtomicReference<File> file = new AtomicReference<>(null);
    private TestSubscriber<Boolean> subscriber = new TestSubscriber<>();
    private CallbackFileChooserFactory fileChooserFactory;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        fileChooserFactory = new CallbackFileChooserFactory(file::get);
        interactor = new ExportIdentityContactInteractor(
            QabelFXMLView.getDefaultResourceBundle(),
            fileChooserFactory
        );
    }

    @Test
    public void exportIdentityTest() throws Exception {
        Identity identity = identityBuilderFactory.factory()
            .withAlias("Test")
            .withEmail("email")
            .withPhone("123456")
            .build();

        file.set(testfile);
        interactor.export(identity, null).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        assertTrue(testfile.exists());
        Contact contact = ContactExportImport.parseContactForIdentity(Files.toString(testfile, Charset.defaultCharset()));
        assertEquals("Test", contact.getAlias());
        assertEquals("email", contact.getEmail());
        assertEquals(identity.getKeyIdentifier(), contact.getKeyIdentifier());
        assertEquals("123456", contact.getPhone());
        assertEquals(identity.getHelloDropUrl(), contact.getDropUrls().toArray()[0]);

        assertEquals(".qco", fileChooserFactory.lastFilterExtension);
        assertEquals("Test.qco", fileChooserFactory.lastDefaultFileName);
        assertEquals("Contact import format (*.qco)", fileChooserFactory.lastFilterName);
        assertEquals("Kontakt exportieren", fileChooserFactory.lastTitle);
    }

    @Test
    public void doesNotExportOnCloseChooser() throws Exception {
        interactor.export(identity, null).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        assertFalse(subscriber.getOnNextEvents().get(0));
    }
}
