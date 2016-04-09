package de.qabel.desktop.repository.sqlite;

import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

public abstract class AbstractSqliteRepository<T> {
    protected ClientDatabase database;
    protected Hydrator<T> hydrator;
    private String queryPrefix;

    public AbstractSqliteRepository(ClientDatabase database, Hydrator<T> hydrator, String tableName) {
        this.database = database;
        this.hydrator = hydrator;
        queryPrefix = "SELECT ";
        queryPrefix += String.join(", ", hydrator.getFields("t"));
        queryPrefix += " FROM " + tableName + " t ";
    }

    protected T find(String condition, Object... params) throws EntityNotFoundExcepion, PersistenceException {
        String query = queryPrefix + " WHERE " + condition + " LIMIT 1";
        try (PreparedStatement statement = database.prepare(query)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i+1, params[i]);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new EntityNotFoundExcepion("no entry for '" + query + "' and " + params + " -> " + params[0]);
                }
                return hydrator.hydrateOne(resultSet);
            }
        } catch (SQLException e) {
            throw new PersistenceException(
                "query failed: '" + query + "' and " + params + "(" + e.getMessage() + ")",
                e
            );
        }
    }

    protected Collection<T> findAll(String condition, Object... params) throws PersistenceException {
        String query = queryPrefix + (condition.isEmpty() ? "" : " WHERE " + condition);
        try (PreparedStatement statement = database.prepare(query)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i+1, params[i]);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return hydrator.hydrateAll(resultSet);
            }
        } catch (SQLException e) {
            throw new PersistenceException(
                "query failed: '" + query + "' (" + e.getMessage() + ")",
                e
            );
        }
    }
}
