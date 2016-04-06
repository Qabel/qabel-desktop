package de.qabel.desktop.repository.sqlite.migration;

import org.junit.After;
import org.junit.Before;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;

public class AbstractSqliteTest {
    protected Connection connection;
    private Path dbFile;

    @Before
    public void setUp() throws Exception {
        dbFile = Files.createTempFile("qabel", "testdb");
        dbFile.toFile().deleteOnExit();
        connection = DriverManager.getConnection("jdbc:sqlite://" + dbFile.toAbsolutePath());
        connection.createStatement().execute("PRAGMA FOREIGN_KEYS = ON");
    }

    @After
    public void tearDown() throws Exception {
        connection.close();
        Files.delete(dbFile);
    }
}
