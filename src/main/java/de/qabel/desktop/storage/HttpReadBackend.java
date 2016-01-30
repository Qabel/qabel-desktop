package de.qabel.desktop.storage;

import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.exceptions.QblStorageNotFound;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HttpReadBackend extends AbstractHttpBackend implements StorageReadBackend {
	protected static final Logger logger = LoggerFactory.getLogger(S3ReadBackend.class.getSimpleName());
	private static final SimpleDateFormat lastModifiedFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

	public HttpReadBackend(String root) {
		super(root);

		logger.info("S3ReadBackend with root address set to " + root);
	}

	public StorageDownload download(String name) throws QblStorageException {
		try {
			return download(name, null);
		} catch (UnmodifiedException e) {
			throw new IllegalStateException(e);
		}
	}

	protected void prepareRequest(HttpRequest request) {

	}

	public StorageDownload download(String name, Long ifModifiedSince) throws QblStorageException, UnmodifiedException {
		logger.info("Downloading " + name);
		URI uri;
		try {
			uri = new URI(this.root).resolve(name);
		} catch (URISyntaxException e) {
			throw new QblStorageException(e);
		}
		HttpGet httpGet = new HttpGet(uri);
		if (ifModifiedSince != null) {
			httpGet.addHeader(HttpHeaders.IF_NONE_MATCH, "*");
			httpGet.addHeader(HttpHeaders.IF_MODIFIED_SINCE, lastModifiedFormat.format(new Date(ifModifiedSince)));
		}
		prepareRequest(httpGet);
		CloseableHttpResponse response;
		try {
			response = httpclient.execute(httpGet);
		} catch (IOException e) {
			throw new QblStorageException(e);
		}
		int status = response.getStatusLine().getStatusCode();
		if ((status == 404) || (status == 403)) {
			throw new QblStorageNotFound("File not found");
		}
		if (status == HttpStatus.SC_NOT_MODIFIED) {
			throw new UnmodifiedException();
		}
		if (status != 200) {
			throw new QblStorageException("Download error");
		}
		long mtime;
		try {
			mtime = lastModifiedFormat.parse(response.getFirstHeader(HttpHeaders.LAST_MODIFIED).getValue()).getTime();
		} catch (ParseException e) {
			mtime = System.currentTimeMillis();
		}
		if (ifModifiedSince != null && ifModifiedSince <= mtime) {
			throw new UnmodifiedException();
		}
		HttpEntity entity = response.getEntity();
		if (entity == null) {
			throw new QblStorageException("No content");
		}
		try {
			InputStream content = entity.getContent();
			return new StorageDownload(content, mtime, entity.getContentLength());
		} catch (IOException e) {
			throw new QblStorageException(e);
		}
	}
}
