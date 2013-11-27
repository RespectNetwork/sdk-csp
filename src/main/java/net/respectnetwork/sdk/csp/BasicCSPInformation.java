package net.respectnetwork.sdk.csp;

import java.io.Serializable;

import xdi2.core.xri3.CloudNumber;

public class BasicCSPInformation implements CSPInformation, Serializable {

	private static final long serialVersionUID = 4621748646529792450L;

	public static final String DEFAULT_RESPECT_NETWORK_REGISTRATION_SERVICE_XDI_ENDPOINT = "http://registration.respectnetwork.net/registration";
	public static final CloudNumber DEFAULT_RESPECT_NETWORK_CLOUD_NUMBER = CloudNumber.create("[@]!:uuid:299089fd-9d81-3c59-2990-89fd9d813c59");

	private CloudNumber cspCloudNumber;
	private String cspSecretToken;
	private String hostingEnvironmentRegistryXdiEndpoint;
	private String hostingEnvironmentCloudBaseXdiEndpoint;
	private String respectNetworkRegistrationServiceXdiEndpoint;
	private CloudNumber respectNetworkCloudNumber;

	public BasicCSPInformation(CloudNumber cspCloudNumber, String cspSecretToken, String hostingEnvironmentRegistryXdiEndpoint, String hostingEnvironmentCloudBaseXdiEndpoint, String respectNetworkRegistrationServiceXdiEndpoint, CloudNumber respectNetworkCloudNumber) {

		this.cspCloudNumber = cspCloudNumber;
		this.cspSecretToken = cspSecretToken;
		this.hostingEnvironmentRegistryXdiEndpoint = hostingEnvironmentRegistryXdiEndpoint;
		this.hostingEnvironmentCloudBaseXdiEndpoint = hostingEnvironmentCloudBaseXdiEndpoint;
		this.respectNetworkRegistrationServiceXdiEndpoint = respectNetworkRegistrationServiceXdiEndpoint;
		this.respectNetworkCloudNumber = respectNetworkCloudNumber;
	}

	public BasicCSPInformation(CloudNumber cspCloudNumber, String cspSecretToken, String hostingEnvironmentRegistryXdiEndpoint, String hostingEnvironmentCloudBaseXdiEndpoint) {

		this.cspCloudNumber = cspCloudNumber;
		this.cspSecretToken = cspSecretToken;
		this.hostingEnvironmentRegistryXdiEndpoint = hostingEnvironmentRegistryXdiEndpoint;
		this.hostingEnvironmentCloudBaseXdiEndpoint = hostingEnvironmentCloudBaseXdiEndpoint;
		this.respectNetworkRegistrationServiceXdiEndpoint = DEFAULT_RESPECT_NETWORK_REGISTRATION_SERVICE_XDI_ENDPOINT;
		this.respectNetworkCloudNumber = DEFAULT_RESPECT_NETWORK_CLOUD_NUMBER;
	}

	/*
	 * Getters and setters
	 */

	public CloudNumber getCspCloudNumber() {

		return this.cspCloudNumber;
	}

	public void setCspCloudNumber(CloudNumber cspCloudNumber) {

		this.cspCloudNumber = cspCloudNumber;
	}

	public String getCspSecretToken() {

		return this.cspSecretToken;
	}

	public void setCspSecretToken(String cspSecretToken) {

		this.cspSecretToken = cspSecretToken;
	}

	public String getHostingEnvironmentRegistryXdiEndpoint() {

		return this.hostingEnvironmentRegistryXdiEndpoint;
	}

	public void setHostingEnvironmentRegistryXdiEndpoint(String hostingEnvironmentRegistryXdiEndpoint) {

		this.hostingEnvironmentRegistryXdiEndpoint = hostingEnvironmentRegistryXdiEndpoint;
	}

	public String getHostingEnvironmentCloudBaseXdiEndpoint() {

		return this.hostingEnvironmentCloudBaseXdiEndpoint;
	}

	public void setHostingEnvironmentCloudBaseXdiEndpoint(String hostingEnvironmentCloudBaseXdiEndpoint) {

		this.hostingEnvironmentCloudBaseXdiEndpoint = hostingEnvironmentCloudBaseXdiEndpoint;
	}

	public String getRespectNetworkRegistrationServiceXdiEndpoint() {

		return this.respectNetworkRegistrationServiceXdiEndpoint;
	}

	public void setRespectNetworkRegistrationServiceXdiEndpoint(String respectNetworkRegistrationServiceXdiEndpoint) {

		this.respectNetworkRegistrationServiceXdiEndpoint = respectNetworkRegistrationServiceXdiEndpoint;
	}

	public CloudNumber getRespectNetworkCloudNumber() {

		return this.respectNetworkCloudNumber;
	}

	public void setRespectNetworkCloudNumber(CloudNumber respectNetworkCloudNumber) {

		this.respectNetworkCloudNumber = respectNetworkCloudNumber;
	}
}
