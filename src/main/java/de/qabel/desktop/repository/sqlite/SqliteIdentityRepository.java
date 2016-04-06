package de.qabel.desktop.repository.sqlite;

import de.qabel.core.config.Identities;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropURL;
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

    public SqliteIdentityRepository(ClientDatabase database, IdentityHydrator identityHydrator) {
        super(database, identityHydrator, TABLE_NAME);
    }

    public SqliteIdentityRepository(ClientDatabase database, EntityManager em) {
        this(
            database,
            new IdentityHydrator(
                new DefaultIdentityFactory(),
                em,
                new SqliteDropUrlRepository(database, new DropURLHydrator()),
                new SqlitePrefixRepository(database)
            )
        );
    }

    @Override
    public Identity find(String id) throws EntityNotFoundExcepion, PersistenceException {
        return find("publicKey = ?", id);
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
        PreparedStatement statement = database.prepare(
            "UPDATE identity SET privateKey=?, publicKey=?, alias=?, email=?, phone=? WHERE id=?"
        );
        int i = 1;
        statement.setString(i++, Hex.toHexString(identity.getPrimaryKeyPair().getPrivateKey()));
        statement.setString(i++, identity.getKeyIdentifier());
        statement.setString(i++, identity.getAlias());
        statement.setString(i++, identity.getEmail());
        statement.setString(i++, identity.getPhone());
        statement.setInt(i++, identity.getId());
        statement.execute();
        if (statement.getUpdateCount() <= 0) {
            throw new PersistenceException("Failed to save identity, nothing happened");
        }

        deleteDropUrls(identity);
        storeDropUrls(identity);
        deletePrefixes(identity);
        storePrefixes(identity);
    }

    private void deleteDropUrls(Identity identity) throws SQLException {
        PreparedStatement dropDrops = database.prepare("DELETE FROM drop_url WHERE identity_id = ?");
        dropDrops.setInt(1, identity.getId());
        dropDrops.execute();
    }
    private void deletePrefixes(Identity identity) throws SQLException {
        PreparedStatement dropPrefixes = database.prepare("DELETE FROM prefix WHERE identity_id = ?");
        dropPrefixes.setInt(1, identity.getId());
        dropPrefixes.execute();
    }

    private synchronized void insert(Identity identity) throws SQLException, PersistenceException {
        PreparedStatement statement = database.prepare(
            "INSERT INTO identity (privateKey, publicKey, alias, email, phone) VALUES (?, ?, ?, ?, ?)"
        );
        int i = 1;
        statement.setString(i++, Hex.toHexString(identity.getPrimaryKeyPair().getPrivateKey()));
        statement.setString(i++, identity.getKeyIdentifier());
        statement.setString(i++, identity.getAlias());
        statement.setString(i++, identity.getEmail());
        statement.setString(i++, identity.getPhone());
        statement.execute();
        if (statement.getUpdateCount() <= 0) {
            throw new PersistenceException("Failed to save identity, nothing happened");
        }

        ResultSet generatedKeys = statement.getGeneratedKeys();
        generatedKeys.next();
        identity.setId(generatedKeys.getInt(1));

        storeDropUrls(identity);
        storePrefixes(identity);

        hydrator.recognize(identity);
    }

    private void storePrefixes(Identity identity) throws SQLException {
        for (String prefix : identity.getPrefixes()) {
            PreparedStatement prefixStatment = database.prepare("INSERT INTO prefix (identity_id, prefix) VALUES (?, ?)");
            prefixStatment.setInt(1, identity.getId());
            prefixStatment.setString(2, prefix);
            prefixStatment.execute();
        }
    }

    private void storeDropUrls(Identity identity) throws SQLException {
        for (DropURL url : identity.getDropUrls()) {
            PreparedStatement dropStatement = database.prepare("INSERT INTO drop_url (identity_id, url) VALUES (?, ?)");
            dropStatement.setInt(1, identity.getId());
            dropStatement.setString(2, url.toString());
            dropStatement.execute();
        }
    }
}
