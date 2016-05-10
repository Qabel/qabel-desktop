package de.qabel.desktop.repository.sqlite;

import de.qabel.desktop.repository.sqlite.migration.*;

import java.sql.*;

public class DesktopClientDatabase extends AbstractClientDatabase {

    public DesktopClientDatabase(Connection connection) {
        super(connection);
    }

    @Override
    public AbstractMigration[] getMigrations(Connection connection) {
        return new AbstractMigration[]{
            new Migration1460367000CreateIdentitiy(connection),
            new Migration1460367005CreateContact(connection),
            new Migration1460367010CreateAccount(connection),
            new Migration1460367015ClientConfiguration(connection),
            new Migration1460367020DropState(connection),
            new Migration1460367025BoxSync(connection),
            new Migration1460367030ShareNotification(connection),
            new Migration1460367035Entity(connection),
            new Migration1460367040DropMessage(connection),
            new Migration1460987825PreventDuplicateContacts(connection)
        };
    }

}
