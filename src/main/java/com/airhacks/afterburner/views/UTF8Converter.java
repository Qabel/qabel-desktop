package com.airhacks.afterburner.views;

import javafx.fxml.LoadException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;


public class UTF8Converter extends ResourceBundle.Control {

	@Override
	public ResourceBundle newBundle
			(String name, Locale locale, String format, ClassLoader loader, boolean reload) throws IOException {

		InputStream stream = null;
		String bundleName = toBundleName(name, locale);
		String resourceName = toResourceName(bundleName, "properties");
		URL url = loader.getResource(resourceName);

		try {
			if (!reload) {
				stream = loader.getResourceAsStream(resourceName);
			} else if (url != null) {
				URLConnection connection = url.openConnection();

				if (connection != null) {
					connection.setUseCaches(false);
					stream = connection.getInputStream();
				}
			}

			if (stream == null) {
				throw new LoadException("failed to load ResourceBundle " + name + " on locale " + locale);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
	}
}

