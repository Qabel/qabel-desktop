package de.qabel.desktop.crashReports;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.HttpClients;
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
	private HttpClient httpClient = HttpClients.createMinimal();

	@Override
	public int sendFeedback(String feedback, String name, String email) throws URISyntaxException, IOException {

		URI uri = new URI("https://rink.hockeyapp.net/api/2/apps/" + APP_ID + "/feedback");
		HttpPost httpPost = new HttpPost(uri);
		httpPost.addHeader("X-HockeyAppToken", TOKEN);

		List<NameValuePair> parameters = new ArrayList<>();
		parameters.add(new BasicNameValuePair("text", feedback));
		httpPost.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));

		HttpResponse response = httpClient.execute(httpPost);
		return response.getStatusLine().getStatusCode();
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
