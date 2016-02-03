package de.qabel.desktop.storage;

import de.qabel.core.accounting.AccountingHTTP;
import de.qabel.core.exceptions.QblInvalidCredentials;
import org.apache.http.HttpRequest;

import java.io.IOException;

public class QabelWriteBackend extends HttpWriteBackend {
	private final AccountingHTTP accountingHTTP;

	public QabelWriteBackend(AccountingHTTP accountingHTTP, String prefix) {
		super(accountingHTTP.buildUri("api/v0/files/" + prefix).toString());
		this.accountingHTTP = accountingHTTP;
	}

	@Override
	protected void prepareRequest(HttpRequest request) {
		super.prepareRequest(request);
		try {
			accountingHTTP.addAuthHeader(request);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (QblInvalidCredentials qblInvalidCredentials) {
			qblInvalidCredentials.printStackTrace();
		}
	}
}
