package de.qabel.desktop;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Collection;

import de.qabel.ackack.event.EventEmitter;
import de.qabel.core.config.*;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.exceptions.QblDropInvalidURL;
import org.apache.commons.cli.*;

import de.qabel.core.drop.DropActor;
import de.qabel.core.module.ModuleManager;
import de.qabel.core.drop.DropURL;
import org.bouncycastle.util.encoders.Hex;

public class QblMain {
	private static final String MODULE_OPT = "module";
	private final EventEmitter emitter;
	private Thread dropActorThread;
	private ContactsActor contactsActor;
	private ConfigActor configActor;

	private DropActor dropActor;
	private Thread contactActorThread;
	private Thread configActorThread;
	public static void main(String[] args) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException,
			InterruptedException, MalformedURLException, QblDropInvalidURL, InvalidKeyException {

		QblMain main = new QblMain();
		main.parse(args);
		main.loadDropServers();
		main.loadContacts();
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
		QblECKeyPair aliceKey = new QblECKeyPair(Hex.decode("77076d0a7318a57d3c16c17251b26645df4c2f87ebc0992ab177fba51db92c2a"));
		QblECKeyPair bobKey = new QblECKeyPair(Hex.decode("5dab087e624a8a4b79e17f8b83800ee66f3bb1292618b6fd1c2f8b27ff88e0eb"));

		Identity alice = new Identity(
				"Alice",
				aliceDropURLs,
				aliceKey
		);

		Collection<DropURL> bobDropURLs = new ArrayList<DropURL>();
		aliceDropURLs.add(new DropURL(
				"http://localhost:6000/123456789012345678901234567890123456789012b"));
		Identity bob = new Identity(
				"Bob",
				bobDropURLs,
				bobKey
		);
		Identities identities = new Identities();
		identities.put(alice);
		identities.put(bob);

		Contact alicesContact = new Contact(alice,aliceDropURLs,aliceKey.getPub());

        alicesContact.addDrop(new DropURL("http://localhost:6000/123456789012345678901234567890123456789012b"));

        Contact bobsContact = new Contact(bob, bobDropURLs, aliceKey.getPub());

        alicesContact.addDrop(new DropURL("http://localhost:6000/123456789012345678901234567890123456789012a"));

		Contacts contacts = new Contacts();
		contacts.put(alicesContact);
		contacts.put(bobsContact);

		// TODO: Remove this once DropActor retrieves contacts from ContactsActor
		this.configActor.writeIdentities(identities.getIdentities().toArray(new Identity[0]));
		this.contactsActor.writeContacts(contacts.getContacts().toArray(new Contact[0]));
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

		servers.put(alicesServer);
		servers.put(bobsServer);

		this.configActor.writeDropServers(servers.getDropServers().toArray(new DropServer[0]));
	}

    /**
     * The application main loop.
     * @throws InterruptedException
     */
	private void run() throws InterruptedException {
		dropActorThread.join();

		moduleManager.shutdown();
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

		String[] moduleStrs = commandLine.getOptionValues(MODULE_OPT);
		if(moduleStrs == null)
			return;
		for (String module : moduleStrs) {
			startModule(module);
		}
	}

    /**
     * Instantiates global DropController and ModuleManager.
     */
	private QblMain() {
		Persistence.setPassword("qabel".toCharArray());
		options.addOption(MODULE_OPT, true, "start a module at loadtime");
		emitter = EventEmitter.getDefault();
		contactsActor = ContactsActor.getDefault();
		configActor = ConfigActor.getDefault();
		configActorThread = new Thread(configActor, "ConfigActor");
		configActorThread.start();
		contactActorThread = new Thread(contactsActor, "ContactsActor");
		contactActorThread.start();
		dropActor = new DropActor(emitter);
		dropActor.setInterval(5000L);
		dropActorThread = new Thread(dropActor, "DropActor");
		dropActorThread.start();
		moduleManager = new ModuleManager(emitter, configActor, contactsActor);
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
