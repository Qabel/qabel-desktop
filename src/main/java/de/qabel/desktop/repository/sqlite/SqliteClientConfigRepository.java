package de.qabel.desktop.repository.sqlite;

import de.qabel.desktop.repository.ClientConfigRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqliteClientConfigRepository implements ClientConfigRepository {
    private ClientDatabase database;

    public SqliteClientConfigRepository(ClientDatabase database) {
        this.database = database;
    }

    @Override
    public String find(String key) throws EntityNotFoundExcepion, PersistenceException {
        try (PreparedStatement statement = database.prepare(
            "SELECT `value` FROM client_configuration WHERE `key` = ? LIMIT 1"
        )) {
            statement.setString(1, key);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new EntityNotFoundExcepion("key not found: " + key);
                }
                return resultSet.getString(1);
            }
        } catch (SQLException e) {
            throw new PersistenceException("failed searching for client configuration with key: " + key, e);
        }
    }

    @Override
    public boolean contains(String key) throws PersistenceException {
        try {
            find(key);
            return true;
        } catch (EntityNotFoundExcepion e) {
            return false;
        }
    }

    @Override
    public void save(String key, String value) throws PersistenceException {
        if (value == null) {
            try (PreparedStatement statement = database.prepare("DELETE FROM client_configuration WHERE `key` = ?")) {
                statement.setString(1, key);
                statement.execute();
            } catch (SQLException e) {
                throw new PersistenceException("failed to delete '" + key + "'");
            }
        } else {
            try (PreparedStatement statement = database.prepare(
                "INSERT INTO client_configuration (`key`, `value`) VALUES (?, ?)"
            )) {
                statement.setString(1, key);
                statement.setString(2, value);
                statement.execute();
            } catch (SQLException e) {
                throw new PersistenceException("failed to save '" + key + "'='" + value + "'", e);
            }
        }
    }
}
