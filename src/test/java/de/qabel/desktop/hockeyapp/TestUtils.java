package de.qabel.desktop.hockeyapp;

import de.qabel.core.accounting.CloseableHttpResponseStub;
import org.apache.http.NameValuePair;
import org.apache.http.entity.BasicHttpEntity;

import java.io.ByteArrayInputStream;
import java.util.List;

public class TestUtils {

    /**
     * @param List<NameValuePair> list
     * @param String              key
     * @return
     */
    protected static String getValueByKey(List<NameValuePair> list, String key) {
        for (NameValuePair entry : list) {
            if (entry.getName().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    protected static CloseableHttpResponseStub createResponseFromString(int statusCode, String responseContent) {
        CloseableHttpResponseStub response = new CloseableHttpResponseStub();
        response.setStatusCode(statusCode);
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(new ByteArrayInputStream(responseContent.getBytes()));
        response.setEntity(entity);

        return response;
    }
}
