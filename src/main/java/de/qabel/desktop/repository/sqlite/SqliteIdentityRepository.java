package de.qabel.desktop.repository.sqlite;

import de.qabel.core.config.Identities;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.factory.DefaultIdentityFactory;
import de.qabel.desktop.repository.EntityManager;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.repository.sqlite.hydrator.DropURLHydrator;
import de.qabel.desktop.repository.sqlite.hydrator.IdentityHydrator;
import org.spongycastle.util.encoders.Hex;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

public class SqliteIdentityRepository extends AbstractSqliteRepository<Identity> implements IdentityRepository {
    private static final String TABLE_NAME = "identity";
    private static final String CONTACT_TABLE_NAME = "contact";
    private final SqliteDropUrlRepository dropUrlRepository;
    private final SqlitePrefixRepository prefixRepository;

    public SqliteIdentityRepository(
        ClientDatabase database,
        IdentityHydrator identityHydrator,
        SqliteDropUrlRepository dropUrlRepository,
        SqlitePrefixRepository prefixRepository
    ) {
        super(database, identityHydrator, TABLE_NAME);
        this.dropUrlRepository = dropUrlRepository;
        this.prefixRepository = prefixRepository;
    }

    public SqliteIdentityRepository(ClientDatabase database, EntityManager em) {
        this(
            database,
            new IdentityHydrator(
                new DefaultIdentityFactory(),
                em,
                new SqliteDropUrlRepository(database, new DropURLHydrator()),
                new SqlitePrefixRepository(database)
            ),
            new SqliteDropUrlRepository(database, new DropURLHydrator()),
            new SqlitePrefixRepository(database)
        );
    }

    @Override
    protected String getQueryPrefix() {
        return "SELECT " + String.join(",", hydrator.getFields("i", "c")) + " " +
            "FROM " + TABLE_NAME + " i " +
            "JOIN contact c ON (i.contact_id = c.id) ";
    }

    @Override
    public Identity find(String keyId) throws EntityNotFoundExcepion, PersistenceException {
        return findBy("c.publicKey=?", keyId);
    }

    @Override
    public Identity find(int id) throws EntityNotFoundExcepion, PersistenceException {
        return findBy("i.id = ?", id);
    }

    @Override
    public Identities findAll() throws EntityNotFoundExcepion, PersistenceException {
        Collection<Identity> all = super.findAll("");
        Identities identities = new Identities();
        for (Identity identity : all) {
            identities.put(identity);
        }
        return identities;
    }

    @Override
    public void save(Identity identity) throws PersistenceException {
        try {
            if (identity.getId() == 0) {
                insert(identity);
            } else {
                update(identity);
            }
        } catch (SQLException e) {
            throw new PersistenceException("failed to save identity: " + e.getMessage(), e);
        }
    }

    private synchronized void update(Identity identity) throws SQLException, PersistenceException {
        int contactId = getOrInsertContactId(identity);
        try (PreparedStatement statement = database.prepare(
            "UPDATE identity SET privateKey=? WHERE id=?"
        )) {
            int i = 1;
            statement.setString(i++, Hex.toHexString(identity.getPrimaryKeyPair().getPrivateKey()));
            statement.setInt(i++, identity.getId());
            statement.execute();
            if (statement.getUpdateCount() <= 0) {
                throw new PersistenceException("Failed to save identity, nothing happened");
            }

            updateContact(contactId, identity);

            dropUrlRepository.delete(contactId);
            dropUrlRepository.store(identity, contactId);
            prefixRepository.delete(identity);
            prefixRepository.store(identity);
        }
    }

    private void updateContact(int contactId, Identity identity) throws SQLException {
        try (PreparedStatement statement = database.prepare(
            "UPDATE contact SET publicKey = ?, alias = ?, email = ?, phone = ? WHERE id = ?"
        )) {
            int i = 1;
            statement.setString(i++, identity.getKeyIdentifier());
            statement.setString(i++, identity.getAlias());
            statement.setString(i++, identity.getEmail());
            statement.setString(i++, identity.getPhone());
            statement.setInt(i++, contactId);
        }
    }

    private synchronized void insert(Identity identity) throws SQLException, PersistenceException {
        int contactId = getOrInsertContactId(identity);

        try (PreparedStatement statement = database.prepare(
            "INSERT INTO identity (contact_id, privateKey) VALUES (?, ?)"
        )) {
            int i = 1;
            statement.setInt(i++, contactId);
            statement.setString(i++, Hex.toHexString(identity.getPrimaryKeyPair().getPrivateKey()));
            statement.execute();
            if (statement.getUpdateCount() <= 0) {
                throw new PersistenceException("Failed to save identity, nothing happened");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                generatedKeys.next();
                identity.setId(generatedKeys.getInt(1));
            }

            dropUrlRepository.store(identity, contactId);
            prefixRepository.store(identity);

            hydrator.recognize(identity);
        }
    }

    private int getOrInsertContactId(Identity identity) throws SQLException, PersistenceException {
        try (PreparedStatement statement = database.prepare(
            "SELECT c.id FROM contact c WHERE c.publicKey = ?"
        )) {
            statement.setString(1, identity.getKeyIdentifier());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }

        try (PreparedStatement statement = database.prepare(
            "INSERT INTO contact (publicKey, alias, email, phone) VALUES (?, ?, ?, ?)"
        )) {
            int i = 1;
            statement.setString(i++, identity.getKeyIdentifier());
            statement.setString(i++, identity.getAlias());
            statement.setString(i++, identity.getEmail());
            statement.setString(i++, identity.getPhone());
            statement.execute();
            try (ResultSet set = statement.getGeneratedKeys()) {
                if (!set.next()) {
                    throw new PersistenceException("failed to insert new contact for identity " + identity);
                }
                return set.getInt(1);
            }
        }
    }
}
