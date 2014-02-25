package net.respectnetwork.sdk.csp;

import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.util.XDIClientUtil;
import xdi2.core.xri3.CloudNumber;
import xdi2.core.xri3.XDI3Segment;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;

public class BasicCSPInformation implements CSPInformation, Serializable {

	private static final long serialVersionUID = 4621748646529792450L;

	private CloudNumber cspCloudNumber;
	private String cspRegistryXdiEndpoint;
	private String cspCloudBaseXdiEndpoint;
	private String cspSecretToken;
	private PrivateKey cspSignaturePrivateKey;
	private CloudNumber rnCloudNumber;
	private String rnRegistrationServiceXdiEndpoint;
	private XDI3Segment rnCspLinkContract;
	private String rnCspSecretToken;

	private XDIDiscoveryClient xdiDiscoveryClient;

	public BasicCSPInformation() {

	}

	public BasicCSPInformation(CloudNumber cspCloudNumber, String cspRegistryXdiEndpoint, String cspCloudBaseXdiEndpoint, String cspSecretToken, PrivateKey cspPrivateKey, CloudNumber rnCloudNumber, String rnRegistrationServiceXdiEndpoint, XDI3Segment rnCspLinkContract, String rnCspSecretToken, XDIDiscoveryClient xdiDiscoveryClient) {

		this.cspCloudNumber = cspCloudNumber;
		this.cspRegistryXdiEndpoint = cspRegistryXdiEndpoint;
		this.cspCloudBaseXdiEndpoint = cspCloudBaseXdiEndpoint;
		this.cspSecretToken = cspSecretToken;
		this.cspSignaturePrivateKey = cspPrivateKey;
		this.rnCloudNumber = rnCloudNumber;
		this.rnRegistrationServiceXdiEndpoint = rnRegistrationServiceXdiEndpoint;
		this.rnCspLinkContract = rnCspLinkContract;
		this.rnCspSecretToken = rnCspSecretToken;

		this.xdiDiscoveryClient = xdiDiscoveryClient;
	}

	public void retrieveSignaturePrivateKey() throws Xdi2ClientException, GeneralSecurityException {

		XDIDiscoveryResult xdiDiscoveryResult = this.getXdiDiscoveryClient().discover(this.getCspCloudNumber().getXri(), null);
		String cspXdiEndpoint = xdiDiscoveryResult.getXdiEndpointUri();

		PrivateKey cspSignaturePrivateKey = XDIClientUtil.retrieveSignaturePrivateKey(this.getCspCloudNumber(), cspXdiEndpoint, this.getCspSecretToken());

		this.setCspSignaturePrivateKey(cspSignaturePrivateKey);
	}

	/*
	 * Getters and setters
	 */

	@Override
	public CloudNumber getCspCloudNumber() {

		return this.cspCloudNumber;
	}

	public void setCspCloudNumber(CloudNumber cspCloudNumber) {

		this.cspCloudNumber = cspCloudNumber;
	}

	@Override
	public String getCspRegistryXdiEndpoint() {

		return this.cspRegistryXdiEndpoint;
	}

	public void setCspRegistryXdiEndpoint(String cspRegistryXdiEndpoint) {

		this.cspRegistryXdiEndpoint = cspRegistryXdiEndpoint;
	}

	@Override
	public String getCspCloudBaseXdiEndpoint() {

		return this.cspCloudBaseXdiEndpoint;
	}

	public void setCspCloudBaseXdiEndpoint(String cspCloudBaseXdiEndpoint) {

		this.cspCloudBaseXdiEndpoint = cspCloudBaseXdiEndpoint;
	}

	@Override
	public String getCspSecretToken() {

		return this.cspSecretToken;
	}

	public void setCspSecretToken(String cspSecretToken) {

		this.cspSecretToken = cspSecretToken;
	}

	@Override
	public PrivateKey getCspSignaturePrivateKey() {

		return this.cspSignaturePrivateKey;
	}

	public void setCspSignaturePrivateKey(PrivateKey cspSignaturePrivateKey) {

		this.cspSignaturePrivateKey = cspSignaturePrivateKey;
	}

	@Override
	public CloudNumber getRnCloudNumber() {

		return this.rnCloudNumber;
	}

	public void setRnCloudNumber(CloudNumber rnCloudNumber) {

		this.rnCloudNumber = rnCloudNumber;
	}

	@Override
	public String getRnRegistrationServiceXdiEndpoint() {

		return this.rnRegistrationServiceXdiEndpoint;
	}

	public void setRnRegistrationServiceXdiEndpoint(String rnRegistrationServiceXdiEndpoint) {

		this.rnRegistrationServiceXdiEndpoint = rnRegistrationServiceXdiEndpoint;
	}

	@Override
	public XDI3Segment getRnCspLinkContract() {

		return this.rnCspLinkContract;
	}

	public void setRnCspLinkContract(XDI3Segment rnCspLinkContract) {

		this.rnCspLinkContract = rnCspLinkContract;
	}

	@Override
	public String getRnCspSecretToken() {

		return this.rnCspSecretToken;
	}

	public void setRnCspSecretToken(String rnCspSecretToken) {

		this.rnCspSecretToken = rnCspSecretToken;
	}

	public XDIDiscoveryClient getXdiDiscoveryClient() {

		return this.xdiDiscoveryClient;
	}

	public void setXdiDiscoveryClient(XDIDiscoveryClient xdiDiscoveryClient) {

		this.xdiDiscoveryClient = xdiDiscoveryClient;
	}
}
