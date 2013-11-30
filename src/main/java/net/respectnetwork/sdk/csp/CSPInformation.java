package net.respectnetwork.sdk.csp;

import xdi2.core.xri3.CloudNumber;

/**
 * This interface represents information required for using the SDK.
 */
public interface CSPInformation {

	/*
	 * Information about the CSP
	 */

	public CloudNumber getCspCloudNumber();
	public String getCspSecretToken();
	public String getCspRegistryXdiEndpoint();
	public String getCspCloudBaseXdiEndpoint();

	/*
	 * Information about RN
	 */

	public CloudNumber getRnCloudNumber();
	public String getRnRegistrationServiceXdiEndpoint();
}
