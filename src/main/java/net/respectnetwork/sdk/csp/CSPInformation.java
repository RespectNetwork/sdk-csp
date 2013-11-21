package net.respectnetwork.sdk.csp;

import xdi2.core.xri3.XDI3Segment;

/**
 * This interface represents information required for using the SDK.
 */
public interface CSPInformation {

	public XDI3Segment getCspCloudNumber();
	public String getCspSecretToken();
	public String getHostingEnvironmentRegistryXdiEndpoint();
	public String getHostingEnvironmentCloudBaseXdiEndpoint();
	public String getRespectNetworkRegistrationServiceXdiEndpoint();
	public XDI3Segment getRespectNetworkCloudNumber();
}
