package net.respectnetwork.sdk.csp.ssl;

public class TLSv1Support {

	static {

		System.setProperty("https.protocols", "TLSv1");
	}
}
