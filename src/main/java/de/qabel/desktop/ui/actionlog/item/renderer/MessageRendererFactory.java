package de.qabel.desktop.ui.actionlog.item.renderer;

public interface MessageRendererFactory {
    MessageRenderer getRenderer(String payloadType);
    boolean hasRenderer(String payloadType);
}
