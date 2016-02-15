package de.qabel.desktop.storage;

import de.qabel.core.accounting.AccountingHTTP;
import de.qabel.core.exceptions.QblInvalidCredentials;
import org.apache.http.HttpRequest;

import java.io.IOException;

public class BlockWriteBackend extends HttpWriteBackend {
	private AccountingHTTP accountingHTTP;

	public BlockWriteBackend(String root, AccountingHTTP accountingHTTP) {
		super(root);
		this.accountingHTTP = accountingHTTP;
	}

	@Override
	protected void prepareRequest(HttpRequest request) {
		super.prepareRequest(request);
		try {
			accountingHTTP.authorize(request);
		} catch (IOException | QblInvalidCredentials e) {
			throw new IllegalStateException("failed to authorize block request: " + e.getMessage(), e);
		}
	}
}
