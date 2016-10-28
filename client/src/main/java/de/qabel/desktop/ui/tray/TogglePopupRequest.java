package de.qabel.desktop.ui.tray;

public class TogglePopupRequest {
    public boolean visible;
    public int x;
    public int y;

    public TogglePopupRequest(boolean visible, int x, int y) {
        this.visible = visible;
        this.x = x;
        this.y = y;
    }
}
