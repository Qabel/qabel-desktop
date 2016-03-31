package de.qabel.desktop.repository;

import de.qabel.desktop.config.ClientConfiguration;

public interface ClientConfigurationRepository {
    ClientConfiguration load();

    void save(ClientConfiguration configuration);
}
