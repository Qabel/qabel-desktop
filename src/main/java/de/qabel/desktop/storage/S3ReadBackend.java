package de.qabel.desktop.storage;

public class S3ReadBackend extends HttpReadBackend {

	S3ReadBackend(String bucket, String prefix) {
		this("https://" + bucket + ".s3.amazonaws.com/" + prefix);
	}

	S3ReadBackend(String root) {
		super(root);
	}
}
