package de.qabel.desktop.ui.actionlog.item.renderer;

import java.util.HashMap;
import java.util.Map;

public class MessageRendererFactory {
	private MessageRenderer fallbackRenderer;
	private Map<String, MessageRenderer> renderers = new HashMap<>();

	public void addRenderer(String payloadType, MessageRenderer renderer) {
        renderers.put(payloadType, renderer);
	}

	public boolean hasRenderer(String payloadType) {
		return renderers.containsKey(payloadType);
	}

	public MessageRenderer getRenderer(String payloadType) {
		MessageRenderer renderer = renderers.get(payloadType);
		if (renderer == null) {
			return fallbackRenderer;
		}
		return renderer;
	}

	public void setFallbackRenderer(MessageRenderer fallbackRenderer) {
		this.fallbackRenderer = fallbackRenderer;
	}
}
