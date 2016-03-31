package de.qabel.desktop.ui.connector;


import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.AbstractBinaryDropMessage;
import de.qabel.core.crypto.BinaryDropMessageV0;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.drop.DropURL;
import de.qabel.core.exceptions.*;
import de.qabel.core.http.DropHTTP;
import de.qabel.core.http.HTTPResult;
import de.qabel.desktop.daemon.NetworkStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class HttpDropConnector implements DropConnector {

	DropHTTP dHTTP;
	private final Logger logger = LoggerFactory.getLogger(HttpDropConnector.class);
	private final NetworkStatus networkStatus;

	public HttpDropConnector(NetworkStatus networkStatus, DropHTTP dHTTP) {
		this.networkStatus = networkStatus;
		this.dHTTP = dHTTP;
	}

	@Override
    public void send(Contact c, DropMessage d) throws QblNetworkInvalidResponseException {
		BinaryDropMessageV0 binaryMessage;

		try {
			binaryMessage = new BinaryDropMessageV0(d);
			final byte[] messageByteArray = binaryMessage.assembleMessageFor(c, (Identity)d.getSender());
			DropURL dropURL = convertCollectionIntoDropUrl(c.getDropUrls());
			HTTPResult<?> dropResult = dHTTP.send(dropURL.getUri(), messageByteArray);
			if (dropResult.getResponseCode() >= 300 || dropResult.getResponseCode() < 200) {
				throw new QblNetworkInvalidResponseException();
			}
		} catch (QblDropPayloadSizeException e) {
			throw new IllegalStateException("drop payload too big: " + e.getMessage(), e);
		}
	}

	@Override
    public List<DropMessage> receive(Identity i, Date since) {
		DropURL d = convertCollectionIntoDropUrl(i.getDropUrls());
		HTTPResult<Collection<byte[]>> result = receiveMessages(since, d);

		if (result.getResponseCode() == 0) {
			networkStatus.offline();
		} else {
			networkStatus.online();
		}

		return createDropMessagesFromHttpResult(result, i);
	}

	private DropURL convertCollectionIntoDropUrl(Set<DropURL> dropUrls) {
		for (DropURL d : dropUrls) {
			return d;
		}
		throw new IllegalArgumentException("No drop URL found");
	}

	private HTTPResult<Collection<byte[]>> receiveMessages(Date sinceDate, DropURL d) {
		return dHTTP.receiveMessages(d.getUri(), sinceDate == null ? 0 : sinceDate.getTime());
	}

	private LinkedList<DropMessage> createDropMessagesFromHttpResult(
			HTTPResult<Collection<byte[]>> result,
			Identity identity) {
		LinkedList<DropMessage> dropMessages = new LinkedList<>();

		for (byte[] message : result.getData()) {
			AbstractBinaryDropMessage binMessage;
			byte binaryFormatVersion = message[0];

			if (binaryFormatVersion != 0) {
				continue;
			}

			try {
				binMessage = new BinaryDropMessageV0(message);
				DropMessage dropMessage = binMessage.disassembleMessage(identity);
				if (dropMessage == null) {
					logger.warn("got empty dropmessage");
					continue;
				}
				dropMessages.add(dropMessage);
			} catch (QblException e) {
				logger.trace("can't create Drop Message from HTTP result: " + e.getMessage());
			}
		}
		return dropMessages;
	}
}
