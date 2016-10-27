package de.qabel.desktop.repository.sqlite;

import de.qabel.core.repository.EntityManager;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.core.repository.sqlite.*;
import de.qabel.desktop.config.BoxSyncConfig;
import de.qabel.desktop.config.factory.DefaultBoxSyncConfigFactory;
import de.qabel.desktop.daemon.sync.worker.index.sqlite.SqliteSyncIndexFactory;
import de.qabel.desktop.repository.BoxSyncRepository;
import de.qabel.desktop.repository.sqlite.hydrator.BoxSyncConfigHydrator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SqliteBoxSyncRepository extends AbstractSqliteRepository<BoxSyncConfig> implements BoxSyncRepository {
    public static final String TABLE_NAME = "box_sync";
    private final List<Consumer<BoxSyncConfig>> addListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<BoxSyncConfig>> deleteListeners = new CopyOnWriteArrayList<>();

    public SqliteBoxSyncRepository(ClientDatabase database, Hydrator<BoxSyncConfig> hydrator) {
        super(database, hydrator, TABLE_NAME);
    }

    public SqliteBoxSyncRepository(ClientDatabase clientDatabase, EntityManager em) {
        this(
            clientDatabase,
            new BoxSyncConfigHydrator(
                em,
                new DefaultBoxSyncConfigFactory(new SqliteSyncIndexFactory()),
                new SqliteIdentityRepository(clientDatabase, em),
                new SqliteAccountRepository(clientDatabase, em)
            )
        );
    }

    @Override
    public List<BoxSyncConfig> findAll() throws PersistenceException {
        return super.findAll("").stream().collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public void save(BoxSyncConfig config) throws PersistenceException {
        if (config.getId() == 0) {
            insert(config);
        } else {
            update(config);
        }
    }

    private void update(BoxSyncConfig config) throws PersistenceException {
        try (PreparedStatement statement = database.prepare(
            "UPDATE box_sync " +
                "SET name = ?, account_id = ?, identity_id = ?, local_path = ?, remote_path = ?, paused = ? " +
                "WHERE id = ?"
        )) {
            int i = 1;
            statement.setString(i++, config.getName());
            statement.setInt(i++, config.getAccount().getId());
            statement.setInt(i++, config.getIdentity().getId());
            statement.setString(i++, config.getLocalPath().toString());
            statement.setString(i++, config.getRemotePath().toString());
            statement.setBoolean(i++, config.isPaused());
            statement.setInt(i++, config.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("failed to update box sync config", e);
        }
    }

    private void insert(BoxSyncConfig config) throws PersistenceException {
        try (PreparedStatement statement = database.prepare(
            "INSERT INTO box_sync (name, account_id, identity_id, local_path, remote_path, paused) VALUES (?,?,?,?,?,?)"
        )) {
            int i = 1;
            statement.setString(i++, config.getName());
            statement.setInt(i++, config.getAccount().getId());
            statement.setInt(i++, config.getIdentity().getId());
            statement.setString(i++, config.getLocalPath().toString());
            statement.setString(i++, config.getRemotePath().toString());
            statement.setBoolean(i++, config.isPaused());
            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                resultSet.next();
                config.setId(resultSet.getInt(1));
            }

            hydrator.recognize(config);
            addListeners.forEach(c -> c.accept(config));
        } catch (SQLException e) {
            throw new PersistenceException("failed to save boxSyncConfig", e);
        }
    }

    @Override
    public void delete(BoxSyncConfig config) throws PersistenceException {
        try (PreparedStatement statement = database.prepare("DELETE FROM " + TABLE_NAME + " WHERE id = ?")) {
            statement.setInt(1, config.getId());
            statement.execute();
            config.setId(0);
            deleteListeners.forEach(c -> c.accept(config));
        } catch (SQLException e) {
            throw new PersistenceException("failed to delete boxSyncConfig", e);
        }
    }

    @Override
    public void onAdd(Consumer<BoxSyncConfig> consumer) {
        addListeners.add(consumer);
    }

    @Override
    public void onDelete(Consumer<BoxSyncConfig> consumer) {
        deleteListeners.add(consumer);
    }
}
