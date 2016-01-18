package de.qabel.desktop.ui.accounting;

import com.google.gson.annotations.Expose;
import java.util.HashMap;


public class GsonIdentity extends GsonEntity {

	@Expose
	private HashMap<String, byte[]> keys = new HashMap<>();

	private  byte[] publicKey;
	private byte[] privateKey;


	public byte[] getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(byte[] publicKey) {
		this.publicKey = publicKey;
	}

	public byte[] getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(byte[] privateKey) {
		this.privateKey = privateKey;
	}

	public void generateKeyStructure() {
		keys.put("private_key", privateKey);
		keys.put("public_key", publicKey);
	}

	public void buildKeyPair() {

		privateKey = keys.get("private_key");
		publicKey = keys.get("public_key");
	}

}
