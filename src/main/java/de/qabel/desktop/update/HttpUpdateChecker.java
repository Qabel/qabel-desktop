package de.qabel.desktop.update;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;


public class HttpUpdateChecker implements UpdateChecker {
	@Override
	public VersionInformation loadInfos() {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet("https://files.qabel.de/etc/versions.json");
		try (CloseableHttpResponse response = (CloseableHttpResponse) client.execute(request)) {
			String body = IOUtils.toString(response.getEntity().getContent());
			return new Gson().fromJson(body, VersionInformation.class);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to check for updates: " + e.getMessage(), e);
		}
	}
}
