package de.qabel.desktop.repository.sqlite;

import de.qabel.core.config.Account;
import de.qabel.desktop.config.factory.DefaultAccountFactory;
import de.qabel.desktop.repository.AccountRepository;
import de.qabel.desktop.repository.EntityManager;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.repository.sqlite.hydrator.AccountHydrator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class SqliteAccountRepository extends AbstractSqliteRepository<Account> implements AccountRepository {
    public static final String TABLE_NAME = "account";

    public SqliteAccountRepository(ClientDatabase database, Hydrator<Account> hydrator) {
        super(database, hydrator, TABLE_NAME);
    }

    public SqliteAccountRepository(ClientDatabase clientDatabase, EntityManager em) {
        this(clientDatabase, new AccountHydrator(em, new DefaultAccountFactory()));
    }

    @Override
    public Account find(String id) throws EntityNotFoundExcepion {
        try {
            return super.findBy("id=?", id);
        } catch (PersistenceException e) {
            throw new EntityNotFoundExcepion("no account with id " + id, e);
        }
    }

    @Override
    public Account find(int id) throws EntityNotFoundExcepion {
        return find(String.valueOf(id));
    }

    @Override
    public List<Account> findAll() throws PersistenceException {
        List<Account> accounts = new LinkedList<>();
        accounts.addAll(super.findAll(""));
        return accounts;
    }

    @Override
    public void save(Account account) throws PersistenceException {
        try {
            Account loaded = findBy("`provider` = ? AND `user` = ?", account.getProvider(), account.getUser());

            try (PreparedStatement statement = database.prepare(
                "UPDATE `account` SET `auth` = ? WHERE ROWID = ?"
            )) {
                statement.setString(1, account.getAuth());
                statement.setInt(2, loaded.getId());
                statement.execute();
                account.setId(loaded.getId());
            } catch (SQLException e) {
                throw new PersistenceException("failed to update account", e);
            }
            return;
        } catch (EntityNotFoundExcepion ignored) {}

        try (PreparedStatement statement = database.prepare(
            "INSERT INTO `account` (`provider`, `user`, `auth`) VALUES (?, ?, ?)"
        )) {
            int i = 1;
            statement.setString(i++, account.getProvider());
            statement.setString(i++, account.getUser());
            statement.setString(i++, account.getAuth());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                account.setId(keys.getInt(1));
            }

            hydrator.recognize(account);
        } catch (SQLException e) {
            throw new PersistenceException(
                "failed to save account " + account.getUser() + "@" + account.getProvider(),
                e
            );
        }
    }
}
