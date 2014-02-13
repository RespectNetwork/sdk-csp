package net.respectnetwork.sdk.csp;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * DO NOT USE THIS IN A PRODUCTION ENVIRONMENT !!
 */
public class TrustAllTrustManager implements X509TrustManager {

	@Override
	public X509Certificate[] getAcceptedIssuers() { return null; }

	@Override
	public void checkClientTrusted(X509Certificate[] certs, String authType) { }

	@Override
	public void checkServerTrusted(X509Certificate[] certs, String authType) { }

	public static void trustAll() {

		SSLContext sslContext;

		try {

			sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, new TrustManager[] { new TrustAllTrustManager() }, null);
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
		} catch (GeneralSecurityException ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
}
