package de.qabel.desktop.ui.accounting.interactor;

import com.airhacks.afterburner.views.QabelFXMLView;
import com.google.common.io.Files;
import de.qabel.core.config.Identity;
import de.qabel.core.config.IdentityExportImport;
import de.qabel.desktop.ui.AbstractControllerTest;
import de.qabel.desktop.ui.util.CallbackFileChooserFactory;
import org.junit.Before;
import org.junit.Test;
import rx.observers.TestSubscriber;

import java.io.File;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExportIdentityInteractorTest extends AbstractControllerTest {
    private static File testfile = new File(Files.createTempDir().getAbsolutePath() + "/tmpfile");
    private ExportIdentityInteractor interactor;
    private AtomicReference<File> file = new AtomicReference<>(null);
    private TestSubscriber<Boolean> subscriber = new TestSubscriber<>();
    private CallbackFileChooserFactory fileChooserFactory;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        fileChooserFactory = new CallbackFileChooserFactory(file::get);
        interactor = new ExportIdentityInteractor(
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
        Identity newIdentity = IdentityExportImport.parseIdentity(Files.toString(testfile, Charset.defaultCharset()));
        assertEquals("Test", newIdentity.getAlias());
        assertEquals("email", newIdentity.getEmail());
        assertEquals(identity.getPrimaryKeyPair(), newIdentity.getPrimaryKeyPair());
        assertEquals("123456", newIdentity.getPhone());
        assertEquals(identity.getHelloDropUrl(), newIdentity.getHelloDropUrl());

        assertEquals(".qid", fileChooserFactory.lastFilterExtension);
        assertEquals("Test.qid", fileChooserFactory.lastDefaultFileName);
        assertEquals("Identity import format (*.qid)", fileChooserFactory.lastFilterName);
        assertEquals("Identität exportieren", fileChooserFactory.lastTitle);
    }

    @Test
    public void doesNotExportOnCloseChooser() throws Exception {
        interactor.export(identity, null).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        assertFalse(subscriber.getOnNextEvents().get(0));
    }
}
