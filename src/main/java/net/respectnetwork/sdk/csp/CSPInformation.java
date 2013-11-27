package net.respectnetwork.sdk.csp;

import xdi2.core.xri3.CloudNumber;

/**
 * This interface represents information required for using the SDK.
 */
public interface CSPInformation {

	public CloudNumber getCspCloudNumber();
	public String getCspSecretToken();
	public String getHostingEnvironmentRegistryXdiEndpoint();
	public String getHostingEnvironmentCloudBaseXdiEndpoint();
	public String getRespectNetworkRegistrationServiceXdiEndpoint();
	public CloudNumber getRespectNetworkCloudNumber();
}
