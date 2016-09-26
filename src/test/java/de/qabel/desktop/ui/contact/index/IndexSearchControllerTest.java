package de.qabel.desktop.ui.contact.index;

import de.qabel.core.config.Contact;
import de.qabel.desktop.ui.AbstractControllerTest;
import org.junit.Test;

import java.util.*;

import static de.qabel.desktop.AsyncUtils.assertAsync;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

public class IndexSearchControllerTest extends AbstractControllerTest {
    private IndexSearchController controller;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Locale.setDefault(Locale.US);

        initController();
    }

    private void initController() {
        IndexSearchView view = new IndexSearchView();
        view.getView();
        controller = view.getPresenter();
        waitUntil(controller::isSubscriptionRunning);
    }

    private void setText(String text) {
        controller.search.setText(text);
        runLaterAndWait(() -> {});
    }

    private String getText() {
        return controller.search.getText();
    }

    @Test
    public void requestsOnlyValidInputs() {
        setText("invalid");
        verifyZeroInteractions(indexService);
    }

    @Test
    public void formatsNumber() throws Exception {
        when(indexService.searchContacts("", "+18002332323")).thenReturn(getSingleResult());
        setText("1 800 2332323");

        assertAsync(this::getText, equalTo("+18002332323"));
        assertAsync(() -> verify(indexService).searchContacts("", "+18002332323"));
        verifyNoMoreInteractions(indexService);
    }

    @Test
    public void formatsEmail() throws Exception {
        when(indexService.searchContacts("test@test2.de", "")).thenReturn(getSingleResult());
        setText("test@test2.de");

        assertAsync(() -> verify(indexService).searchContacts("test@test2.de", ""));
        verifyNoMoreInteractions(indexService);
    }

    @Test
    public void showsResults() throws Exception {
        stub(indexService.searchContacts("test@test.de", "")).toReturn(getResults(2));
        setText("test@test.de");

        assertAsync(controller.resultContainer.getChildren()::size, equalTo(2));
    }

    private List<Contact> getSingleResult() {
        return getResults(1);
    }

    private List<Contact> getResults(int amount) {
        List<Contact> result = new LinkedList<>();
        for (int i = 0; i < amount; i++) {
            result.add(identity.toContact());
        }
        return result;
    }

}
