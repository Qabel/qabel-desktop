package de.qabel.desktop.storage;

import de.qabel.core.accounting.AccountingHTTP;
import de.qabel.core.accounting.AccountingProfile;
import de.qabel.core.config.AccountingServer;
import de.qabel.core.crypto.QblECKeyPair;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.junit.Assert.fail;

public class BoxVolumeBlockTest extends BoxVolumeTest {
	@Override
	protected void setUpVolume() {
		try {
			QblECKeyPair keyPair = new QblECKeyPair();
			AccountingServer server = new AccountingServer(new URI("http://localhost:9696"), "testuser", "testuser");
			AccountingHTTP accountingHTTP = new AccountingHTTP(server, new AccountingProfile());

			List<String> prefixes = accountingHTTP.getPrefixes();
			if (prefixes.isEmpty()) {
				accountingHTTP.createPrefix();
				prefixes = accountingHTTP.getPrefixes();
			}
			prefix = prefixes.get(0);

			String root = "http://localhost:9697/api/v0/files/" + prefix;
			volume = new BoxVolume(
					new BlockReadBackend(root, accountingHTTP),
					new BlockWriteBackend(root, accountingHTTP),
					keyPair,
					deviceID,
					new File(System.getProperty("java.io.tmpdir")),
					prefix
			);
			volume2 = new BoxVolume(
					new BlockReadBackend(root, accountingHTTP),
					new BlockWriteBackend(root, accountingHTTP),
					keyPair,
					deviceID,
					new File(System.getProperty("java.io.tmpdir")),
					prefix
			);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Override
	protected void cleanVolume() throws IOException {

	}
}
