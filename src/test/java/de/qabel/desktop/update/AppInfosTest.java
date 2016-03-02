package de.qabel.desktop.update;

import com.google.gson.Gson;
import org.junit.Test;

import static org.junit.Assert.*;

public class AppInfosTest {
	@Test
	public void testStructure() {
		String response = "{\n" +
				"    \"appinfos\": {\n" +
				"        \"android\": {\n" +
				"            \"currentAppVersion\": 91,\n" +
				"            \"minimumAppVersion\": 89,\n" +
				"            \"downloadURL\": \"http://m.qabel.de/apps/android.html\"\n" +
				"        },\n" +
				"        \"desktop\": {\n" +
				"            \"currentAppVersion\": 91,\n" +
				"            \"minimumAppVersion\": 89,\n" +
				"            \"downloadURL\": \"http://m.qabel.de/apps/desktop\"\n" +
				"        }\n" +
				"    }\n" +
				"}";

		VersionInformation info = new Gson().fromJson(response, VersionInformation.class);

		assertNotNull(info);
	}
}