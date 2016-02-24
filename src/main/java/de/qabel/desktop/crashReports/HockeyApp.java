package de.qabel.desktop.crashReports;


import com.google.gson.Gson;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HockeyApp implements CrashReportHandler {
	private static final String APP_ID = "3b119dc227334d2d924e4e134c72aadc";
	private static final String TOKEN = "350b097ef0964b17a0f3907050de309d";
	private DefaultHttpClient httpClient = new DefaultHttpClient();
	private Gson gson =  new Gson();

	@Override
	public int sendFeedback(String feedback) throws URISyntaxException, IOException {

		List<NameValuePair> urlParameters = new ArrayList<>();
		urlParameters.add(new BasicNameValuePair("text", feedback));

		URI uri = new URI("https://rink.hockeyapp.net/api/2/apps/" + APP_ID + "/feedback");
		HttpPost httpPost = new HttpPost(uri);
		httpPost.addHeader("X-HockeyAppToken", TOKEN);
		httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));
		CloseableHttpResponse response = this.httpClient.execute(httpPost);
		return response.getStatusLine().getStatusCode();
	}

	@Override
	public int sendStacktrace(String feedback, String stacktrace) throws URISyntaxException, IOException {

		String log = createlog(stacktrace);

		List<NameValuePair> urlParameters = new ArrayList<>();
		urlParameters.add(new BasicNameValuePair("log", log));
		urlParameters.add(new BasicNameValuePair("description", feedback));

		URI uri = new URI("https://rink.hockeyapp.net/api/2/apps/" + APP_ID + "/crashes/upload");
		HttpPost httpPost = new HttpPost(uri);
		String json = gson.toJson(urlParameters);
		StringEntity entity = new StringEntity(json);
		entity.setContentType("application/json");
		httpPost.setEntity(entity);

		CloseableHttpResponse response = httpClient.execute(httpPost);
		return response.getStatusLine().getStatusCode();
	}

	private String createlog(String stacktrace) {
		Date date = new Date();
		StringBuilder log = new StringBuilder();

		log.append("Package: de.qabel.desktop\n");
		log.append("Version: 1\n");
		log.append("OS: " + System.getProperty("os.name") + " / " +
				System.getProperty("os.arch") + " / " +
				System.getProperty("os.version") + "\n"
		);
		log.append("Manufacturer: " + System.getProperty("java.vendor") + "\n");
		log.append("Model: " + System.getProperty("java.version") + "\n");
		log.append("Date: " + date + "\n");
		log.append("\n");
		log.append(stacktrace);

		return log.toString();
	}
}
