package de.qabel.desktop;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Collection;

import de.qabel.ackack.event.EventEmitter;
import de.qabel.core.exceptions.QblDropInvalidURL;
import de.qabel.core.module.ModuleThread;
import org.apache.commons.cli.*;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Contacts;
import de.qabel.core.config.DropServer;
import de.qabel.core.config.DropServers;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblKeyFactory;
import de.qabel.core.crypto.QblPrimaryKeyPair;
import de.qabel.core.drop.DropActor;
import de.qabel.core.module.Module;
import de.qabel.core.module.ModuleManager;
import de.qabel.core.drop.DropURL;

public class QblMain {
	private static final String MODULE_OPT = "module";

	private DropActor dropController;

	public static void main(String[] args) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException,
			InterruptedException, MalformedURLException, QblDropInvalidURL, InvalidKeyException {

		QblMain main = new QblMain();
		main.parse(args);
		main.loadContacts();
		main.loadDropServers();
		main.startModules();
		main.run();
	}

    /**
     * Creates global available identities and puts them into contact with each other.
	 * @throws MalformedURLException, QblDropInvalidURL, InvalidKeyException
     */
	private void loadContacts() throws MalformedURLException, QblDropInvalidURL, InvalidKeyException {
		Collection<DropURL> aliceDropURLs = new ArrayList<DropURL>();
		aliceDropURLs.add(new DropURL(
				"http://localhost:6000/123456789012345678901234567890123456789012a"));
		QblPrimaryKeyPair alicesKey = QblKeyFactory.getInstance()
				.generateQblPrimaryKeyPair();
		Identity alice = new Identity(
				"Alice",
				aliceDropURLs,
				alicesKey
		);

		Collection<DropURL> bobDropURLs = new ArrayList<DropURL>();
		aliceDropURLs.add(new DropURL(
				"http://localhost:6000/123456789012345678901234567890123456789012b"));
		QblPrimaryKeyPair bobsKey = QblKeyFactory.getInstance()
				.generateQblPrimaryKeyPair();
		Identity bob = new Identity(
				"Bob",
				bobDropURLs,
				bobsKey
		);

		Contact alicesContact = new Contact(alice);
        alicesContact.setPrimaryPublicKey(bobsKey.getQblPrimaryPublicKey());
		alicesContact.addEncryptionPublicKey(bobsKey.getQblEncPublicKey());
		alicesContact.addSignaturePublicKey(bobsKey.getQblSignPublicKey());
        alicesContact.getDropUrls().add(new DropURL("http://localhost:6000/123456789012345678901234567890123456789012b"));

        Contact bobsContact = new Contact(bob);
        bobsContact.setPrimaryPublicKey(alicesKey.getQblPrimaryPublicKey());
		bobsContact.addEncryptionPublicKey(alicesKey.getQblEncPublicKey());
		bobsContact.addSignaturePublicKey(alicesKey.getQblSignPublicKey());
        alicesContact.getDropUrls().add(new DropURL("http://localhost:6000/123456789012345678901234567890123456789012a"));

		Contacts contacts = new Contacts();
		contacts.add(alicesContact);
		contacts.add(bobsContact);

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
		                "http://localhost:6000/123456789012345678901234567890123456789012a"));

        DropServer bobsServer = new DropServer();
        bobsServer
                .setUrl(new URL(
                        "http://localhost:6000/123456789012345678901234567890123456789012b"));

        DropServers servers = new DropServers();

		servers.add(alicesServer);
		servers.add(bobsServer);

		dropController.setDropServers(servers);
	}

    /**
     * The application main loop.
     * @throws InterruptedException
     */
	private void run() throws InterruptedException {
		dropController.run();
		for (ModuleThread module : moduleManager.getModules()) {
			module.getModule().stopModule();
		}
	}

	private ModuleManager moduleManager;
	private CommandLine commandLine;

    /**
     * Starts all modules given on the commandline.
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
	private void startModules() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		for (String module : commandLine.getOptionValues(MODULE_OPT)) {
			startModule(module);
		}
	}

    /**
     * Instantiates global DropController and ModuleManager.
     */
	private QblMain() {
		options.addOption(MODULE_OPT, true, "start a module at loadtime");
		EventEmitter emitter = EventEmitter.getDefault();
		dropController = new DropActor(emitter);
		moduleManager = new ModuleManager();
		moduleManager.setDropActor(dropController);
	}

    /**
     * Parses commandline arguments.
     * @param args Which module(s) should be loaded. Usage: "path to jar":"full class name of module"
     * @return
     */
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

    /**
     * Uses the module manager to start a module.
     * @param module The module that gets started.
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
	private void startModule(String module) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		String[] moduleParts = module.split(":", 2);
		moduleManager.startModule(new File(moduleParts[0]), moduleParts[1]);
	}

	private Options options = new Options();

}
