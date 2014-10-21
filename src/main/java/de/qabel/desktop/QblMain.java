package de.qabel.desktop;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Security;

import org.apache.commons.cli.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Contacts;
import de.qabel.core.config.DropServer;
import de.qabel.core.config.DropServers;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblEncPublicKey;
import de.qabel.core.crypto.QblKeyFactory;
import de.qabel.core.crypto.QblPrimaryKeyPair;
import de.qabel.core.crypto.QblPrimaryPublicKey;
import de.qabel.core.crypto.QblSignPublicKey;
import de.qabel.core.drop.DropController;
import de.qabel.core.module.Module;
import de.qabel.core.module.ModuleManager;

public class QblMain {
	private static final String MODULE_OPT = "module";

	private DropController dropController;

	public static void main(String[] args) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException,
			InterruptedException, MalformedURLException {
		System.setSecurityManager(null);


		QblMain main = new QblMain();
		main.parse(args);
		main.loadContacts();
		main.loadDropServers();
		main.startModules();
		main.run();
	}

	private void loadContacts() throws MalformedURLException {
		Identity identity = new Identity(
				"Alice",
				new URL(
						"http://localhost:8000/123456789012345678901234567890123456789012c"));
		QblPrimaryKeyPair myKey = QblKeyFactory.getInstance()
				.generateQblPrimaryKeyPair();
		identity.setPrimaryKeyPair(myKey);
		
		Contacts contacts = new Contacts();
		Contact contact = new Contact(identity);
		QblPrimaryKeyPair qpkp = QblKeyFactory.getInstance()
				.generateQblPrimaryKeyPair();
		QblPrimaryPublicKey qppk = qpkp.getQblPrimaryPublicKey();
		QblEncPublicKey qepk = qpkp.getQblEncPublicKey();
		QblSignPublicKey qspk = qpkp.getQblSignPublicKey();

        contact.setPrimaryPublicKey(qppk);
        contact.setEncryptionPublicKey(qepk);
        contact.setSignaturePublicKey(qspk);

		contact.getDropUrls()
				.add(new URL(
						"http://localhost:8000/123456789012345678901234567890123456789012d"));
		contacts.getContacts().add(contact);
		dropController.setContacts(contacts);
	}

	private void loadDropServers() throws MalformedURLException {
		DropServers servers = new DropServers();
		// Generate DropServer instances here and
		// put them into servers. Example:
		DropServer dropServer = new DropServer();
		dropServer
				.setUrl(new URL(
						"http://localhost:8000/123456789012345678901234567890123456789012d"));
        servers.getDropServer().add(dropServer);
		dropController.setDropServers(servers);
	}

	private void run() throws InterruptedException {
		while (true) {
			dropController.retrieve();
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
		}
		for (Module module : moduleManager.getModules()) {
			module.join();
		}
	}

	private ModuleManager moduleManager;
	private CommandLine commandLine;

	private void startModules() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		for (String module : commandLine.getOptionValues(MODULE_OPT)) {
			startModule(module);
		}
	}

	private QblMain() {
		options.addOption(MODULE_OPT, true, "start a module at loadtime");
		dropController = new DropController();
		moduleManager = new ModuleManager();
		moduleManager.setDropController(dropController);
	}

	private boolean parse(String... args) {
		CommandLineParser parser = new GnuParser();
		try {
			commandLine = parser.parse(options, args);
		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(HelpFormatter.DEFAULT_ARG_NAME, options);
			return false;
		}

		return true;
	}

	private void startModule(String module) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		String[] moduleParts = module.split(":", 2);
		moduleManager.startModule(new File(moduleParts[0]), moduleParts[1]);
	}

	private Options options = new Options();

}
