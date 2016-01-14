package com.airhacks.afterburner.views;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;


public class UTF8Converter extends ResourceBundle.Control {

	@Override
	public ResourceBundle newBundle
			(String name, Locale locale, String format, ClassLoader loader, boolean reload) {

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
				return null;
			}

			return new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}

