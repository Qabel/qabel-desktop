package de.qabel.desktop.ui.accounting;

import com.google.gson.annotations.Expose;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropURL;

import java.util.HashMap;


public class GsonIdentity extends GsonEntity {

	@Expose
	private HashMap<String, byte[]> keys = new HashMap<>();

	private byte[] publicKey;
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

	public GsonIdentity fromIdentity(Identity i) {
		GsonIdentity gi = new GsonIdentity();
		gi.setEmail(i.getEmail());
		gi.setPhone(i.getPhone());
		gi.setAlias(i.getAlias());
		gi.setCreated(i.getCreated());
		gi.setUpdated(i.getUpdated());
		gi.setDeleted(i.getDeleted());
		gi.setPublicKey(i.getEcPublicKey().getKey());
		gi.setPrivateKey(i.getPrimaryKeyPair().getPrivateKey());
		for (DropURL d : i.getDropUrls()) {
			gi.addDropUrl(d.getUri().toString());
		}
		return gi;
	}

}
