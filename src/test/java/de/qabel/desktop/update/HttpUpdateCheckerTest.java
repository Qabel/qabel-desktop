package de.qabel.desktop.update;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HttpUpdateCheckerTest {
	private HttpUpdateChecker checker;
	private HttpClientStub client;
	private StubHttpResponse response;

	@Before
	public void setUp() {
		client = new HttpClientStub();
		checker = new HttpUpdateChecker(client);
		response = new StubHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "OK");
		client.response = response;
	}

	private String generateBody(String currentVersion, String minimalVersion) {
		return "{\n" +
				"    \"appinfos\": {\n" +
				"        \"android\": {\n" +
				"            \"currentAppVersion\": 91,\n" +
				"            \"minimumAppVersion\": 89,\n" +
				"            \"downloadURL\": \"http://m.qabel.de/apps/android.html\"\n" +
				"        },\n" +
				"        \"desktop\": {\n" +
				"            \"currentAppVersion\": \"" + currentVersion + "\",\n" +
				"            \"minimumAppVersion\": \"" + minimalVersion + "\",\n" +
				"            \"downloadURL\": \"http://m.qabel.de/apps/desktop\"\n" +
				"        }\n" +
				"    }\n" +
				"}";
	}

	@Test
	public void testParsesVersion() {
		setResponse("0.2.0", "0.1.0");
		VersionInformation version = checker.loadInfos();
		assertEquals("0.2.0", version.getAppinfos().getDesktop().getCurrentAppVersion());
		assertEquals("0.1.0", version.getAppinfos().getDesktop().getMinimumAppVersion());
	}

	@Test
	public void validatesCurrentVersion() {
		setResponse("0.2.0", "0.1.0");
		assertTrue(checker.isAllowed("0.2.0"));
		assertTrue(checker.isCurrent("0.2.0"));
	}

	@Test
	public void allowsIntermediateVersion() {
		setResponse("0.2.9", "0.2.0");
		assertTrue(checker.isAllowed("0.2.1"));
		assertFalse(checker.isCurrent("0.2.1"));
	}

	@Test
	public void deniesOutdatedVersion() {
		setResponse("0.2.9", "0.2.0");
		assertFalse(checker.isAllowed("0.1.9"));
	}

	@Test
	public void allowsLeastAllowedVersion() {
		setResponse("0.2.9", "0.2.0");
		assertTrue(checker.isAllowed("0.2.0"));
	}

	@Test
	public void passesFreshReleasesAsCurrentVersion() {
		setResponse("0.2.9", "0.2.0");
		assertTrue(checker.isCurrent("0.2.10"));
	}

	private void setResponse(String current, String minimal) {
		response.setEntity(new InputStreamEntity(new ByteArrayInputStream(generateBody(current, minimal).getBytes())));
	}

	private class StubHttpResponse extends BasicHttpResponse implements CloseableHttpResponse {

		public StubHttpResponse(ProtocolVersion ver, int code, String reason) {
			super(ver, code, reason);
		}

		@Override
		public void close() throws IOException {

		}
	}

	private class HttpClientStub implements HttpClient {
		public CloseableHttpResponse response;

		@Override
		public HttpParams getParams() {
			return null;
		}

		@Override
		public ClientConnectionManager getConnectionManager() {
			return null;
		}

		@Override
		public HttpResponse execute(HttpUriRequest request) throws IOException {
			CloseableHttpResponse response = this.response;
			this.response = null;
			return response;
		}

		@Override
		public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException {
			return null;
		}

		@Override
		public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException {
			return null;
		}

		@Override
		public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException {
			return null;
		}

		@Override
		public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException {
			return null;
		}

		@Override
		public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException {
			return null;
		}

		@Override
		public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) throws IOException {
			return null;
		}

		@Override
		public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException {
			return null;
		}
	}
}
