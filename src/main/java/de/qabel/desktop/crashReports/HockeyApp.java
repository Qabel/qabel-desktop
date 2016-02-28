package de.qabel.desktop.crashReports;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HockeyApp implements CrashReportHandler {
	private static final String APP_ID = "3b119dc227334d2d924e4e134c72aadc";
	private static final String TOKEN = "350b097ef0964b17a0f3907050de309d";
	private RequestConfig config = RequestConfig.custom().
			setConnectTimeout(2 * 1000).build();
	private HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
	private HttpResponse response;

	@Override
	public void sendFeedback(String feedback, String name, String email) throws URISyntaxException, IOException {

		URI uri = new URI("https://rink.hockeyapp.net/api/2/apps/" + APP_ID + "/feedback");
		HttpPost httpPost = new HttpPost(uri);
		httpPost.addHeader("X-HockeyAppToken", TOKEN);

		List<NameValuePair> parameters = new ArrayList<>();
		parameters.add(new BasicNameValuePair("text", feedback));
		parameters.add(new BasicNameValuePair("name", name));
		parameters.add(new BasicNameValuePair("email", email));
		httpPost.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));


		new Thread() {
			public void run() {
				try {
					response = httpClient.execute(httpPost);
					if (!(response.getStatusLine().getStatusCode() <= 205)) {
						String str = "Illegal response code from hockey app server - response code: " + response.getStatusLine().getStatusCode();
						throw new IOException(str);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	@Override
	public int sendStacktrace(String feedback, String stacktrace) throws URISyntaxException, IOException {

		String log = createLog(stacktrace);

		URI uri = new URI("https://rink.hockeyapp.net/api/2/apps/" + APP_ID + "/crashes/upload");
		HttpPost httpPost = new HttpPost(uri);

		HttpEntity entity = MultipartEntityBuilder.create()
				.addPart("log", new ByteArrayBody(log.getBytes(), "log"))
				.addPart("description", new ByteArrayBody(feedback.getBytes(), "description"))
				.build();
		httpPost.setEntity(entity);

		HttpResponse response = httpClient.execute(httpPost);
		return response.getStatusLine().getStatusCode();
	}

	private String createLog(String stacktrace) {
		Date date = new Date();
		StringBuilder log = new StringBuilder();

		log.append("Package: de.qabel.desktop\n");
		log.append("Version: 1\n");
		log.append("OS: " + System.getProperty("os.name") + " / ");
		log.append(System.getProperty("os.arch") + " / ");
		log.append(System.getProperty("os.version") + "\n");
		log.append("Manufacturer: " + System.getProperty("java.vendor") + "\n");
		log.append("Model: " + System.getProperty("java.version") + "\n");
		log.append("Date: " + date + "\n");
		log.append("\n");
		log.append(stacktrace);

		return log.toString();
	}
}
