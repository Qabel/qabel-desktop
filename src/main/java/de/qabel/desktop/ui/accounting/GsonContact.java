package de.qabel.desktop.ui.accounting;

import com.google.gson.annotations.Expose;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Entity;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropURL;


public class GsonContact extends GsonEntity {


	@Expose
	private byte[] publicKey;

	public byte[] getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(byte[] publicKey) {
		this.publicKey = publicKey;
	}

	public GsonContact fromEntity(Entity entity) {
		GsonContact gc = new GsonContact();

		if (entity instanceof Contact) {
			Contact c = (Contact) entity;
			gc.setEmail(c.getEmail());
			gc.setPhone(c.getPhone());
			gc.setAlias(c.getAlias());
		} else {
			Identity i = (Identity) entity;
			gc.setEmail(i.getEmail());
			gc.setPhone(i.getPhone());
			gc.setAlias(i.getAlias());
		}

		gc.setCreated(entity.getCreated());
		gc.setUpdated(entity.getUpdated());
		gc.setDeleted(entity.getDeleted());
		gc.setPublicKey(entity.getEcPublicKey().getKey());
		for (DropURL d : entity.getDropUrls()) {
			gc.addDropUrl(d.getUri().toString());
		}
		return gc;
	}
}




