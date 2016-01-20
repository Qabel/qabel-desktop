package de.qabel.desktop.ui.accounting;

import com.google.gson.annotations.Expose;
import java.util.HashMap;


public class GsonIdentity extends GsonEntity {

	@Expose
	private HashMap<String, byte[]> keys = new HashMap<>();

	private  byte[] publicKey;
	private byte[] privateKey;


	public byte[] getPublicKey() {
		publicKey = keys.get("public_key");
		return publicKey;
	}

	public void setPublicKey(byte[] publicKey) {
		this.publicKey = publicKey;
		keys.put("public_key", publicKey);

	}

	public byte[] getPrivateKey() {
		privateKey = keys.get("private_key");
		return privateKey;
	}

	public void setPrivateKey(byte[] privateKey) {
		this.privateKey = privateKey;
		keys.put("private_key", privateKey);
	}
}
