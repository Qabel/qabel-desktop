package de.qabel.desktop.hockeyapp;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;

public class HockeyAppClientTest {
    public static final String BASE_URI = "https://rink.hockeyapp.net/api/2/apps/";
    public static final String APP_ID = "3b119dc227334d2d924e4e134c72aadc";
    public static final String TOKEN = "350b097ef0964b17a0f3907050de309d";

    HttpClient httpClient = HttpClients.createMinimal();


    @Test
    public void testBuildApiUri(){

        this.buildApiUri("/somewhere");
    }

    public void buildApiUri(String apiCallPath){

    }
}
