package de.qabel.desktop.hockeyapp;

import org.apache.http.NameValuePair;

import java.util.List;

public class TestUtils {

    /**
     *
     * @param List<NameValuePair> list
     * @param String key
     * @return String / null
     */
    protected static String getValueByKey(List<NameValuePair> list, String key) {
        for(NameValuePair entry : list) {
            if(entry.getName().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
