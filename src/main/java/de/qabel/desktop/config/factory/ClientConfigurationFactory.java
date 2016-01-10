package de.qabel.desktop.config.factory;

import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.config.DefaultClientConfiguration;

public class ClientConfigurationFactory {
	public ClientConfiguration createClientConfiguration() {
		return new DefaultClientConfiguration();
	}
}
