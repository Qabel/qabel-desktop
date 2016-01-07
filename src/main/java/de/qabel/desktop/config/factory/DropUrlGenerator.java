package de.qabel.desktop.config.factory;

import de.qabel.core.config.DropServer;
import de.qabel.core.drop.DropURL;

import java.net.URI;
import java.net.URISyntaxException;

public class DropUrlGenerator {
	private DropServer dropServer;

	public DropUrlGenerator(String dropServerUrl) throws URISyntaxException {
		this.dropServer = new DropServer(new URI(dropServerUrl), null, true);
	}

	public DropURL generateUrl() {
		try {
			return new DropURL(dropServer);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to generate dropUrl:" + e.getMessage(), e);
		}
	}
}
