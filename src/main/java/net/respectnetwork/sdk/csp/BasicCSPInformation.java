package net.respectnetwork.sdk.csp;

import java.io.Serializable;

import xdi2.core.xri3.CloudNumber;

public class BasicCSPInformation implements CSPInformation, Serializable {

	private static final long serialVersionUID = 4621748646529792450L;

	public static final String DEFAULT_RESPECT_NETWORK_REGISTRATION_SERVICE_XDI_ENDPOINT = "http://registration-dev.respectnetwork.net/registration";
	public static final CloudNumber DEFAULT_RESPECT_NETWORK_CLOUD_NUMBER = CloudNumber.create("[@]!:uuid:299089fd-9d81-3c59-2990-89fd9d813c59");

	private CloudNumber cspCloudNumber;
	private String cspSecretToken;
	private String cspRegistryXdiEndpoint;
	private String cspCloudBaseXdiEndpoint;

	private CloudNumber rnCloudNumber;
	private String rnRegistrationServiceXdiEndpoint;

	public BasicCSPInformation(CloudNumber cspCloudNumber, String cspSecretToken, String cspRegistryXdiEndpoint, String cspCloudBaseXdiEndpoint, CloudNumber rnCloudNumber, String rnRegistrationServiceXdiEndpoint) {

		this.cspCloudNumber = cspCloudNumber;
		this.cspSecretToken = cspSecretToken;
		this.cspRegistryXdiEndpoint = cspRegistryXdiEndpoint;
		this.cspCloudBaseXdiEndpoint = cspCloudBaseXdiEndpoint;

		this.rnCloudNumber = rnCloudNumber;
		this.rnRegistrationServiceXdiEndpoint = rnRegistrationServiceXdiEndpoint;
	}

	public BasicCSPInformation(CloudNumber cspCloudNumber, String cspSecretToken, String cspRegistryXdiEndpoint, String cspCloudBaseXdiEndpoint) {

		this.cspCloudNumber = cspCloudNumber;
		this.cspSecretToken = cspSecretToken;
		this.cspRegistryXdiEndpoint = cspRegistryXdiEndpoint;
		this.cspCloudBaseXdiEndpoint = cspCloudBaseXdiEndpoint;

		this.rnCloudNumber = DEFAULT_RESPECT_NETWORK_CLOUD_NUMBER;
		this.rnRegistrationServiceXdiEndpoint = DEFAULT_RESPECT_NETWORK_REGISTRATION_SERVICE_XDI_ENDPOINT;
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

	public String getCspRegistryXdiEndpoint() {

		return this.cspRegistryXdiEndpoint;
	}

	public void setCspRegistryXdiEndpoint(String cspRegistryXdiEndpoint) {

		this.cspRegistryXdiEndpoint = cspRegistryXdiEndpoint;
	}

	public String getCspCloudBaseXdiEndpoint() {

		return this.cspCloudBaseXdiEndpoint;
	}

	public void setCspCloudBaseXdiEndpoint(String cspCloudBaseXdiEndpoint) {

		this.cspCloudBaseXdiEndpoint = cspCloudBaseXdiEndpoint;
	}

	public String getRnRegistrationServiceXdiEndpoint() {

		return this.rnRegistrationServiceXdiEndpoint;
	}

	public void setRnRegistrationServiceXdiEndpoint(String rnRegistrationServiceXdiEndpoint) {

		this.rnRegistrationServiceXdiEndpoint = rnRegistrationServiceXdiEndpoint;
	}

	public CloudNumber getRnCloudNumber() {

		return this.rnCloudNumber;
	}

	public void setRnCloudNumber(CloudNumber rnCloudNumber) {

		this.rnCloudNumber = rnCloudNumber;
	}
}
