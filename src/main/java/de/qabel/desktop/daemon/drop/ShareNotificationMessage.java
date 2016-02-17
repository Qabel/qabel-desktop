package de.qabel.desktop.daemon.drop;

import com.google.gson.Gson;
import de.qabel.core.crypto.QblECPublicKey;
import org.spongycastle.util.encoders.Hex;

public class ShareNotificationMessage {
	private String url;
	private String key;
	private String message;

	public ShareNotificationMessage(String url, String key, String message) {
		this.url = url;
		this.key = key;
		this.message = message;
	}

	public static ShareNotificationMessage fromJson(String jsonMessage) {
		Gson gson = new Gson();
		return gson.fromJson(jsonMessage, ShareNotificationMessage.class);
	}

	public String toJson() {
		return new Gson().toJson(this);
	}

	public String getUrl() {
		return url;
	}

	public QblECPublicKey getKey() {
		return new QblECPublicKey(Hex.decode(key));
	}

	public String getHexKey() {
		return key;
	}

	public String getMessage() {
		return message;
	}
}
