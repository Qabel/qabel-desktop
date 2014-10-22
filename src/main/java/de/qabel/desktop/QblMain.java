package de.qabel.desktop;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.cli.*;

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


		QblMain main = new QblMain();
		main.parse(args);
		main.loadContacts();
		main.loadDropServers();
		main.startModules();
		main.run();
	}

	private void loadContacts() throws MalformedURLException {
		Identity alice = new Identity(
				"Alice",
				new URL(
						"http://localhost:8000/123456789012345678901234567890123456789012a"));
		QblPrimaryKeyPair alicesKey = QblKeyFactory.getInstance()
				.generateQblPrimaryKeyPair();
		alice.setPrimaryKeyPair(alicesKey);

        Identity bob = new Identity(
                "Bob",
                new URL(
                        "http://localhost:8000/123456789012345678901234567890123456789012b"));
        QblPrimaryKeyPair bobsKey = QblKeyFactory.getInstance()
                .generateQblPrimaryKeyPair();
        bob.setPrimaryKeyPair(bobsKey);

		Contact alicesContact = new Contact(alice);
        alicesContact.setPrimaryPublicKey(bobsKey.getQblPrimaryPublicKey());
        alicesContact.setEncryptionPublicKey(bobsKey.getQblEncPublicKey());
        alicesContact.setSignaturePublicKey(bobsKey.getQblSignPublicKey());
        alicesContact.getDropUrls().add(new URL("http://localhost:8000/123456789012345678901234567890123456789012b"));

        Contact bobsContact = new Contact(bob);
        bobsContact.setPrimaryPublicKey(alicesKey.getQblPrimaryPublicKey());
        bobsContact.setEncryptionPublicKey(alicesKey.getQblEncPublicKey());
        bobsContact.setSignaturePublicKey(alicesKey.getQblSignPublicKey());
        alicesContact.getDropUrls().add(new URL("http://localhost:8000/123456789012345678901234567890123456789012a"));

        Contacts contacts = new Contacts();
        contacts.getContacts().add(alicesContact);
        contacts.getContacts().add(bobsContact);

		dropController.setContacts(contacts);
	}

    /**
     * Generate DropServer instances here and
     * put them into global available servers.
     * @throws MalformedURLException
     */
	private void loadDropServers() throws MalformedURLException {
		DropServer alicesServer = new DropServer();
        alicesServer
				.setUrl(new URL(
						"http://localhost:8000/123456789012345678901234567890123456789012a"));

        DropServer bobsServer = new DropServer();
        bobsServer
                .setUrl(new URL(
                        "http://localhost:8000/123456789012345678901234567890123456789012b"));

        DropServers servers = new DropServers();

        servers.getDropServer().add(alicesServer);
        servers.getDropServer().add(bobsServer);

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
