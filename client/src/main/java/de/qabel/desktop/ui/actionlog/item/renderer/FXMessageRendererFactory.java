package de.qabel.desktop.ui.actionlog.item.renderer;

import java.util.HashMap;
import java.util.Map;

public class FXMessageRendererFactory implements MessageRendererFactory {
    private FXMessageRenderer fallbackRenderer;
    private Map<String, FXMessageRenderer> renderers = new HashMap<>();

    public void addRenderer(String payloadType, FXMessageRenderer renderer) {
        renderers.put(payloadType, renderer);
    }

    public boolean hasRenderer(String payloadType) {
        return renderers.containsKey(payloadType);
    }

    public FXMessageRenderer getRenderer(String payloadType) {
        FXMessageRenderer renderer = renderers.get(payloadType);
        if (renderer == null) {
            return fallbackRenderer;
        }
        return renderer;
    }

    public void setFallbackRenderer(FXMessageRenderer fallbackRenderer) {
        this.fallbackRenderer = fallbackRenderer;
    }
}
