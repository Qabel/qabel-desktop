package de.qabel.desktop.storage;

import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.exceptions.QblStorageNotFound;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.InputStreamBody;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class HttpWriteBackend extends AbstractHttpBackend implements StorageWriteBackend {
	public HttpWriteBackend(String root) {
		super(root);
	}

	@Override
	public long upload(String name, InputStream content) throws QblStorageException {
		URI uri;
		try {
			uri = new URI(this.root).resolve(name);
		} catch (URISyntaxException e) {
			throw new QblStorageException(e);
		}
		File f = new File("tmp/bla");
		try {
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Uploading to " + uri.toString());
		HttpPost httpPost = new HttpPost(uri);
		prepareRequest(httpPost);
		HttpEntity entity = null;
		entity = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.RFC6532)
				.addPart("file", new InputStreamBody(content,  name))
				.build();
		httpPost.setEntity(entity);
		CloseableHttpResponse response;
		try {
			response = httpclient.execute(httpPost);
		} catch (IOException e) {
			throw new QblStorageException(e);
		}
		int status = response.getStatusLine().getStatusCode();
		if ((status == 404) || (status == 403)) {
			throw new QblStorageNotFound("File not found");
		}
		if (status >= 300) {
			throw new QblStorageException("Upload error");
		}
		return System.currentTimeMillis();
	}

	protected void prepareRequest(HttpRequest request) {

	}

	@Override
	public void delete(String name) throws QblStorageException {
		URI uri;
		try {
			uri = new URI(this.root + '/' + name);
		} catch (URISyntaxException e) {
			throw new QblStorageException(e);
		}
		HttpDelete httpDelete = new HttpDelete(uri);
		prepareRequest(httpDelete);
		CloseableHttpResponse response;
		try {
			response = httpclient.execute(httpDelete);
		} catch (IOException e) {
			throw new QblStorageException(e);
		}
		int status = response.getStatusLine().getStatusCode();
		if ((status == 404) || (status == 403)) {
			throw new QblStorageNotFound("File not found");
		}
		if (status != 200) {
			throw new QblStorageException("Deletion error");
		}
	}
}
