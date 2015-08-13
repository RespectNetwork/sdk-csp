package net.respectnetwork.sdk.csp;

import java.security.PrivateKey;

import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;

/**
 * This interface represents information required for using the SDK.
 */
public interface CSPInformation {

	/*
	 * Information about the CSP
	 */

	public CloudNumber getCspCloudNumber();
	public String getCspRegistryXdiEndpoint();
	public String getCspCloudBaseXdiEndpoint();
	public String getCspSecretToken();
	public PrivateKey getCspSignaturePrivateKey();

	/*
	 * Information about RN
	 */

	public CloudNumber getRnCloudNumber();
	public String getRnRegistrationServiceXdiEndpoint();
	public XDIAddress getRnCspLinkContract();
	public String getRnCspSecretToken();
}
