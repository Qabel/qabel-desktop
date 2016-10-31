package de.qabel.desktop.ui.tray;

import org.junit.Test;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.qabel.desktop.AsyncUtils.assertAsync;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TrayIconMouseAdapterTest {
    private AtomicBoolean front = new AtomicBoolean();
    private AtomicBoolean shown = new AtomicBoolean();
    private TrayIconMouseAdapter adapter = new TrayIconMouseAdapter(() -> shown.set(true), () -> front.set(true));

    @Test
    public void bringsToFrontOnSingleLeftClick() {
        adapter.mouseClicked(mouseEvent(MouseEvent.BUTTON1, 1));

        assertTrue(front.get());
        assertFalse(shown.get());
    }

    @Test
    public void showsAppOnDoubleLeftClick() {
        adapter.mouseClicked(mouseEvent(MouseEvent.BUTTON1, 2));

        assertTrue(shown.get());
        assertFalse(front.get());
    }

    @Test
    public void showsPopupOnRightClick() {
        adapter.setPopupCloseCheckDelay(0);
        adapter.setPopupCloseCheckPeriod(250);
        adapter.mouseClicked(mouseEvent(MouseEvent.BUTTON3, 1));

        assertAsync(adapter.getPopup()::isVisible, equalTo(true));
        assertFalse(shown.get());
        assertFalse(front.get());

        assertAsync(adapter.getPopup()::isVisible, equalTo(false));
    }

    private MouseEvent mouseEvent(int buttonId, int clickCount) {
        return new MouseEvent(
            new JButton(),
            0,
            0,
            0,
            0,
            0,
            clickCount,
            false,
            buttonId
        );
    }
}
