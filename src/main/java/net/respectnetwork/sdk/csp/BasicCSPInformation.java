package net.respectnetwork.sdk.csp;

import java.io.Serializable;

import xdi2.core.xri3.CloudNumber;
import xdi2.core.xri3.XDI3Segment;

public class BasicCSPInformation implements CSPInformation, Serializable {

	private static final long serialVersionUID = 4621748646529792450L;

	private CloudNumber cspCloudNumber;
	private String cspSecretToken;
	private String cspRegistryXdiEndpoint;
	private String cspCloudBaseXdiEndpoint;
	private CloudNumber rnCloudNumber;
	private String rnRegistrationServiceXdiEndpoint;
	private XDI3Segment rnCspLinkContract;

	public BasicCSPInformation() {

	}

	public BasicCSPInformation(CloudNumber cspCloudNumber, String cspSecretToken, String cspRegistryXdiEndpoint, String cspCloudBaseXdiEndpoint, CloudNumber rnCloudNumber, String rnRegistrationServiceXdiEndpoint, XDI3Segment rnCspLinkContract) {

		this.cspCloudNumber = cspCloudNumber;
		this.cspSecretToken = cspSecretToken;
		this.cspRegistryXdiEndpoint = cspRegistryXdiEndpoint;
		this.cspCloudBaseXdiEndpoint = cspCloudBaseXdiEndpoint;
		this.rnCloudNumber = rnCloudNumber;
		this.rnRegistrationServiceXdiEndpoint = rnRegistrationServiceXdiEndpoint;
		this.rnCspLinkContract = rnCspLinkContract;
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

	public CloudNumber getRnCloudNumber() {

		return this.rnCloudNumber;
	}

	public void setRnCloudNumber(CloudNumber rnCloudNumber) {

		this.rnCloudNumber = rnCloudNumber;
	}

	public String getRnRegistrationServiceXdiEndpoint() {

		return this.rnRegistrationServiceXdiEndpoint;
	}

	public void setRnRegistrationServiceXdiEndpoint(String rnRegistrationServiceXdiEndpoint) {

		this.rnRegistrationServiceXdiEndpoint = rnRegistrationServiceXdiEndpoint;
	}

	public XDI3Segment getRnCspLinkContract() {

		return this.rnCspLinkContract;
	}

	public void setRnCspLinkContract(XDI3Segment rnCspLinkContract) {

		this.rnCspLinkContract = rnCspLinkContract;
	}
}
