package de.qabel.desktop;

import de.qabel.ackack.event.EventEmitter;
import de.qabel.core.config.*;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.drop.DropActor;
import de.qabel.core.drop.DropURL;
import de.qabel.core.module.ModuleManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

public class QblRESTServerTest {

	private static final String DB_NAME = "QblHelloWorldModuleTest.sqlite";
	private static final String BOB_DROP_URL = "http://localhost:6000/123456789012345678901234567890123456789012b";
	public static final int PORT = 9696;
	private ModuleManager moduleManager;
	private QblRESTServer restServer;
	private DropActor dropActor;
	private ResourceActor resourceActor;
	private String requestOutput;

	@Before
	public void setUp() throws Exception {
		Persistence<String> persistence = new SQLitePersistence(DB_NAME, "qabel".toCharArray());
		resourceActor = new ResourceActor(persistence, EventEmitter.getDefault());
		moduleManager = new ModuleManager(EventEmitter.getDefault(), resourceActor);
		restServer = new QblRESTServer(PORT, resourceActor, dropActor, moduleManager);

		Collection<DropURL> bobDropURLs = new ArrayList<>();
		bobDropURLs.add(new DropURL(BOB_DROP_URL));

		Identity alice = new Identity("Alice", null, new QblECKeyPair());
		Identity bob = new Identity("Bob", bobDropURLs, new QblECKeyPair());

		Identities identities = new Identities();
		identities.put(alice);
		moduleManager.getResourceActor().writeIdentities(identities.getIdentities().toArray(new Identity[0]));

		Contact bobAsContactForAlice = new Contact(alice, "Bob", bobDropURLs, bob.getEcPublicKey());

		Contacts contacts = new Contacts();
		contacts.put(bobAsContactForAlice);
		moduleManager.getResourceActor().writeContacts(contacts.getContacts().toArray(new Contact[0]));

		dropActor = new DropActor(resourceActor, EventEmitter.getDefault());
		Thread dropActorThread = new Thread(dropActor);
		dropActor.setInterval(500);
		dropActorThread.start();
	}

	@After
	public void tearDown() throws InterruptedException {
		restServer.stop();
		File persistenceTestDB = new File(DB_NAME);
		if (persistenceTestDB.exists()) {
			persistenceTestDB.delete();
		}
	}

	private int sendRequest(String ressource, String method) throws IOException {
		String address = "http://localhost:" + PORT + "/" + ressource;
		URL url = new URL(address);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod(method);
		conn.setRequestProperty("Accept", "application/json");

		BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));

		StringBuffer output = new StringBuffer();
		String out;
		while ((out = br.readLine()) != null) {
			output.append(out);
		}

		conn.disconnect();
		requestOutput = output.toString();
		return conn.getResponseCode();
	}

	@org.junit.Test
	public void testSetUp() throws Exception {
		restServer.run();
		Assert.assertEquals(200, sendRequest("status", "GET"));
		Assert.assertEquals("{status: \"running\"}", requestOutput);
	}

}