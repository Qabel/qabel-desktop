package de.qabel.desktop.config.factory;

import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.drop.DropURL;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;

public class IdentityBuilder {
	private DropUrlGenerator dropUrlGenerator;

	private String alias;
	private QblECKeyPair keyPair;
	private List<DropURL> dropUrls;

	@Inject
	public IdentityBuilder(DropUrlGenerator dropUrlGenerator) {
		this.dropUrlGenerator = dropUrlGenerator;
	}

	public IdentityBuilder withAlias(String alias) {
		this.alias = alias;
		return this;
	}

	public IdentityBuilder dropAt(DropURL dropUrl) {
		dropUrls = new LinkedList<>();
		dropUrls.add(dropUrl);
		return this;
	}

	public IdentityBuilder encryptWith(QblECKeyPair keyPair) {
		this.keyPair = keyPair;
		return this;
	}

	public Identity build() {
		if (dropUrls == null || dropUrls.isEmpty()) {
			dropAt(dropUrlGenerator.generateUrl());
		}
		if (keyPair == null) {
			keyPair = new QblECKeyPair();
		}

		return new Identity(alias, dropUrls, keyPair);
	}
}
