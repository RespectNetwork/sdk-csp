package net.respectnetwork.sdk.csp.ssl;

public class TLSv1Support {

	public static void supportTLSv1() {

		System.setProperty("https.protocols", "TLSv1");
	}
}
