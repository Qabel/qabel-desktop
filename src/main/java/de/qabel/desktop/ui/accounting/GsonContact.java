package de.qabel.desktop.ui.accounting;

import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;


public class GsonContact extends GsonEntity {


	@Expose
	public byte[] publicKey;


	public byte[] getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(byte[] publicKey) {
		this.publicKey = publicKey;
	}


}
