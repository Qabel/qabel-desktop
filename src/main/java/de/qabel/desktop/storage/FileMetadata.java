package de.qabel.desktop.storage;

import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.desktop.exceptions.QblStorageException;

import java.io.File;
import java.io.IOException;
import java.sql.*;

public class FileMetadata extends AbstractMetadata {
	private static final String[] initSql = {
			"CREATE TABLE spec_version (" +
					" version INTEGER PRIMARY KEY )",
			"CREATE TABLE file (" +
					" owner BLOB NOT NULL," +
					" block VARCHAR(255) NOT NULL," +
					" name VARCHAR(255) NULL PRIMARY KEY," +
					" size LONG NOT NULL," +
					" mtime LONG NOT NULL," +
					" key BLOB NOT NULL )",
			"INSERT INTO spec_version (version) VALUES(0)"
	};

	public static FileMetadata openExisting(File path) {
		try {
			Connection connection = DriverManager.getConnection(JDBC_PREFIX + path.getAbsolutePath());
			connection.setAutoCommit(true);
			return new FileMetadata(connection, path);
		} catch (SQLException e) {
			throw new RuntimeException("Cannot open database!", e);
		}
	}

	public static FileMetadata openNew(QblECPublicKey owner, BoxFile boxFile, File tmpDir) throws QblStorageException {
		try {
			File path = File.createTempFile("dir", "db", tmpDir);

			Connection connection = DriverManager.getConnection(JDBC_PREFIX + path.getAbsolutePath());
			connection.setAutoCommit(true);
			return new FileMetadata(connection, path, owner, boxFile);
		} catch (SQLException e) {
			throw new RuntimeException("Cannot open database!", e);
		} catch (IOException e) {
			throw new QblStorageException(e);
		}

	}

	public FileMetadata(Connection connection, File path) {
		super(connection, path);
	}

	public FileMetadata(Connection connection, File path, QblECPublicKey owner, BoxFile boxFile) throws QblStorageException, SQLException {
		this(connection, path);

		initDatabase();
		insertFile(owner, boxFile);
	}

	@Override
	protected String[] getInitSql() {
		return initSql;
	}

	private void insertFile(QblECPublicKey owner, BoxFile boxFile) throws QblStorageException {
		try (PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO file (owner, block, name, size, mtime, key) VALUES(?, ?, ?, ?, ?, ?)")) {
			statement.setBytes(1, owner.getKey());
			statement.setString(2, boxFile.block);
			statement.setString(3, boxFile.name);
			statement.setLong(4, boxFile.size);
			statement.setLong(5, boxFile.mtime);
			statement.setBytes(6, boxFile.key);
			if (statement.executeUpdate() != 1) {
				throw new QblStorageException("Failed to insert file");
			}

		} catch (SQLException e) {
			logger.error("Could not insert file " + boxFile.name);
			throw new QblStorageException(e);
		}
	}

	BoxExternalFile getFile() throws QblStorageException {
		try (Statement statement = connection.createStatement()) {
			ResultSet rs = statement.executeQuery("SELECT owner, block, name, size, mtime, key FROM file LIMIT 1");
			if (rs.next()) {
				byte[] ownerKey = rs.getBytes(1);
				String block = rs.getString(2);
				String name = rs.getString(3);
				long size = rs.getLong(4);
				long mtime = rs.getLong(5);
				byte[] key = rs.getBytes(6);
				return new BoxExternalFile(new QblECPublicKey(ownerKey), block, name, size, mtime, key);
			}
			return null;
		} catch (SQLException e) {
			throw new QblStorageException(e);
		}
	}
}
