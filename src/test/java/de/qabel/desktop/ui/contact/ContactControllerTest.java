package de.qabel.desktop.ui.contact;

import de.qabel.core.config.Account;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.storage.*;
import de.qabel.desktop.ui.AbstractControllerTest;
import de.qabel.desktop.ui.remotefs.LazyBoxFolderTreeItem;
import de.qabel.desktop.ui.remotefs.RemoteFSController;
import de.qabel.desktop.ui.remotefs.RemoteFSView;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TreeItem;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class ContactControllerTest extends AbstractControllerTest {


	private ContactController controller = new ContactController();

	@Test
	public void injectlTest() {
		Locale.setDefault(new Locale("de", "DE"));
		ContactView view = new ContactView();
		Identity i = new Identity("test", null, new QblECKeyPair());
		clientConfiguration.selectIdentity(i);
		clientConfiguration.setAccount(new Account("Provider","user","auth"));
		controller = (ContactController) view.getPresenter();

	}

}
