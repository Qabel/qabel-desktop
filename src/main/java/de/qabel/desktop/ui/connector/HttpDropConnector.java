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

import java.util.*;

public class HttpDropConnector implements Connector {

	DropHTTP dHTTP = new DropHTTP();


	public void send(Contact c, DropMessage d) throws QblNetworkInvalidResponseException {
		BinaryDropMessageV0 binaryMessage = null;

		try {
			binaryMessage = new BinaryDropMessageV0(d);
		} catch (QblDropPayloadSizeException e) {
			e.printStackTrace();
		}
		final byte[] messageByteArray = binaryMessage.assembleMessageFor(c);
		DropURL dropURL = convertCollectionIntoDropUrl(c.getDropUrls());
		HTTPResult<?> dropResult = dHTTP.send(dropURL.getUri(), messageByteArray);

		if (dropResult.getResponseCode() != 200 &&
				dropResult.getResponseCode() != 201 &&
				dropResult.getResponseCode() != 202 &&
				dropResult.getResponseCode() != 204
				) {
			throw new QblNetworkInvalidResponseException();
		}
	}

	public List<DropMessage> receive(Identity i, Date siceDate) {
		DropURL d = convertCollectionIntoDropUrl(i.getDropUrls());
		HTTPResult<Collection<byte[]>> result = receiveMessages(siceDate, d);

		return createDropMessagesFromHttpResult(result, i);
	}


	private DropURL convertCollectionIntoDropUrl(Set<DropURL> dropUrls) {
		for (DropURL d : dropUrls) {
			return d;
		}
		throw new NullPointerException();
	}

	private HTTPResult<Collection<byte[]>> receiveMessages(Date sinceDate, DropURL d) {
		return dHTTP.receiveMessages(d.getUri(), sinceDate == null ? 0 : sinceDate.getTime());
	}

	private LinkedList<DropMessage> createDropMessagesFromHttpResult(
			HTTPResult<Collection<byte[]>> result,
			Identity identity) {
		LinkedList<DropMessage> dropMessages = new LinkedList<>();

		for (byte[] message : result.getData()) {
			AbstractBinaryDropMessage binMessage = null;
			byte binaryFormatVersion = message[0];

			if (binaryFormatVersion != 0) {
				continue;
			}

			try {
				binMessage = new BinaryDropMessageV0(message);
			} catch (QblVersionMismatchException | QblDropInvalidMessageSizeException e) {
				e.printStackTrace();
			}

			DropMessage dropMessage = null;

			try {
				dropMessage = binMessage.disassembleMessage(identity);
			} catch (QblSpoofedSenderException e) {
				e.printStackTrace();
			}
			dropMessages.add(dropMessage);

		}
		return dropMessages;
	}
}
