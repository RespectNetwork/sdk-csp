package net.respectnetwork.sdk.csp;

import xdi2.core.xri3.CloudNumber;
import xdi2.core.xri3.XDI3Segment;

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

	/*
	 * Information about RN
	 */

	public CloudNumber getRnCloudNumber();
	public String getRnRegistrationServiceXdiEndpoint();
	public XDI3Segment getRnCspLinkContract();
	public String getRnCspSecretToken();
}
