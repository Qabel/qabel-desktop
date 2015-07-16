package de.qabel.desktop;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Collection;

import de.qabel.ackack.event.EventEmitter;
import de.qabel.core.config.*;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.exceptions.QblDropInvalidURL;
import de.qabel.core.exceptions.QblInvalidEncryptionKeyException;
import org.apache.commons.cli.*;

import de.qabel.core.drop.DropActor;
import de.qabel.core.module.ModuleManager;
import de.qabel.core.drop.DropURL;

public class QblMain {
	private static final String MODULE_OPT = "module";
	private static final String ALICE_DROP_URL = "http://localhost:6000/123456789012345678901234567890123456789012a";
	private static final String BOB_DROP_URL = "http://localhost:6000/123456789012345678901234567890123456789012b";

	private final EventEmitter emitter;
	private Thread dropActorThread;
	private ResourceActor resourceActor;

	private DropActor dropActor;
	private Thread resourceActorThread;
	public static void main(String[] args) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException,
			InterruptedException, URISyntaxException, QblDropInvalidURL, InvalidKeyException {

		QblMain main = new QblMain();
		main.parse(args);
		main.loadDropServers();
		main.loadContactsAndIdentities();
		main.startModules();
		main.run();
	}

    /**
     * Creates global available identities and puts them into contact with each other.
	 * @throws URISyntaxException, QblDropInvalidURL, InvalidKeyException
     */
	private void loadContactsAndIdentities() throws URISyntaxException, QblDropInvalidURL, InvalidKeyException {
		Collection<DropURL> aliceDropURLs = new ArrayList<>();
		aliceDropURLs.add(new DropURL(ALICE_DROP_URL));

		Collection<DropURL> bobDropURLs = new ArrayList<>();
		bobDropURLs.add(new DropURL(BOB_DROP_URL));

		Identity alice = new Identity("Alice", aliceDropURLs, new QblECKeyPair());
		Identity bob = new Identity("Bob", bobDropURLs, new QblECKeyPair());

		Identities identities = new Identities();
		identities.put(alice);
		identities.put(bob);
		this.resourceActor.writeIdentities(identities.getIdentities().toArray(new Identity[0]));

		Contact aliceAsContactForBob = new Contact(bob, "Alice", aliceDropURLs, alice.getEcPublicKey());
		Contact bobAsContactForAlice = new Contact(alice, "Bob", bobDropURLs, bob.getEcPublicKey());

		Contacts contacts = new Contacts();
		contacts.put(aliceAsContactForBob);
		contacts.put(bobAsContactForAlice);
		this.resourceActor.writeContacts(contacts.getContacts().toArray(new Contact[0]));
	}

    /**
     * Generate DropServer instances here and
     * put them into global available servers.
     * @throws URISyntaxException
     */
	private void loadDropServers() throws URISyntaxException {
		DropServer alicesServer = new DropServer();
		alicesServer.setUri(new URI(ALICE_DROP_URL));

		DropServer bobsServer = new DropServer();
		bobsServer.setUri(new URI(BOB_DROP_URL));

		DropServers servers = new DropServers();
		servers.put(alicesServer);
		servers.put(bobsServer);

		this.resourceActor.writeDropServers(servers.getDropServers().toArray(new DropServer[0]));
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
		Persistence<String> persistence = null;
		try {
			persistence = new SQLitePersistence("qabel-desktop.sqlite", "qabel".toCharArray());
		} catch (QblInvalidEncryptionKeyException e) {
			// Can currently not happen due to the static password
		}
		emitter = EventEmitter.getDefault();
		resourceActor = new ResourceActor(persistence, emitter);
		options.addOption(MODULE_OPT, true, "start a module at loadtime");
		resourceActorThread = new Thread(resourceActor, "ConfigActor");
		resourceActorThread.start();
		dropActor = new DropActor(resourceActor, emitter);
		dropActor.setInterval(5000L);
		dropActorThread = new Thread(dropActor, "DropActor");
		dropActorThread.start();
		moduleManager = new ModuleManager(emitter, resourceActor);
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
