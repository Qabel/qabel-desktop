package de.qabel.desktop.storage;

import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.exceptions.QblStorageNotFound;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class HttpWriteBackend extends AbstractHttpStorageBackend implements StorageWriteBackend {
    public HttpWriteBackend(String root) throws URISyntaxException {
        super(root);
    }

    @Override
    public long upload(String name, InputStream content) throws QblStorageException {
        logger.info("Uploading " + name);
        HttpPost httpPost;
        try {
            URI uri = this.root.resolve(name);
            httpPost = new HttpPost(uri);
            prepareRequest(httpPost);
            httpPost.setEntity(new InputStreamEntity(content));

            try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                int status = response.getStatusLine().getStatusCode();
                if ((status == 404) || (status == 403)) {
                    throw new QblStorageNotFound("File not found");
                }
                if (status >= 300) {
                    throw new QblStorageException("Upload error");
                }
            }
            return System.currentTimeMillis();
        } catch (IOException e) {
            throw new QblStorageException(e);
        }
    }

    @Override
    public void delete(String name) throws QblStorageException {
        logger.info("Deleting " + name);
        URI uri;
        CloseableHttpResponse response;

        try {
            uri = this.root.resolve(name);
            HttpDelete httpDelete = new HttpDelete(uri);
            prepareRequest(httpDelete);

            response = httpclient.execute(httpDelete);
        } catch (IOException e) {
            throw new QblStorageException(e);
        }
        int status = response.getStatusLine().getStatusCode();
        if ((status == 404) || (status == 403)) {
            throw new QblStorageNotFound("File not found");
        }
        if (status >= 300) {
            throw new QblStorageException("Deletion error");
        }
    }
}
