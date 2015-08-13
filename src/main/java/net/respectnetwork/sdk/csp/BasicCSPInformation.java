package net.respectnetwork.sdk.csp;

import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.util.XDIClientUtil;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;

public class BasicCSPInformation implements CSPInformation, Serializable {

	/** CLass Logger */
	private static final Logger logger = LoggerFactory
			.getLogger(BasicCSPInformation.class);
	
	private static final long serialVersionUID = 4621748646529792450L;

	private CloudNumber cspCloudNumber;
	private String cspRegistryXdiEndpoint;
	private String cspCloudBaseXdiEndpoint;
	private String cspSecretToken;
	private PrivateKey cspSignaturePrivateKey;
	private CloudNumber rnCloudNumber;
	private String rnRegistrationServiceXdiEndpoint;
	private XDIAddress rnCspLinkContract;
	private String rnCspSecretToken;

	private XDIDiscoveryClient xdiDiscoveryClient;

	@Override
	public String toString(){
		
		return new String("CSP Cloud Number : " + cspCloudNumber.toString() + ", cspRegistryXdiEndpoint : " + cspRegistryXdiEndpoint +
				", rnRegistrationServiceXdiEndpoint : " + rnRegistrationServiceXdiEndpoint + ", cspCloudBaseXdiEndpoint : " + cspCloudBaseXdiEndpoint);
	}
	public BasicCSPInformation() {

	}

	public BasicCSPInformation(CloudNumber cspCloudNumber, String cspRegistryXdiEndpoint, String cspCloudBaseXdiEndpoint, String cspSecretToken, PrivateKey cspSignaturePrivateKey, CloudNumber rnCloudNumber, String rnRegistrationServiceXdiEndpoint, XDIAddress rnCspLinkContract, String rnCspSecretToken, XDIDiscoveryClient xdiDiscoveryClient) {

		this.cspCloudNumber = cspCloudNumber;
		this.cspRegistryXdiEndpoint = cspRegistryXdiEndpoint;
		this.cspCloudBaseXdiEndpoint = cspCloudBaseXdiEndpoint;
		this.cspSecretToken = cspSecretToken;
		this.cspSignaturePrivateKey = cspSignaturePrivateKey;
		this.rnCloudNumber = rnCloudNumber;
		this.rnRegistrationServiceXdiEndpoint = rnRegistrationServiceXdiEndpoint;
		this.rnCspLinkContract = rnCspLinkContract;
		this.rnCspSecretToken = rnCspSecretToken;

		this.xdiDiscoveryClient = xdiDiscoveryClient;
		
	}

	public void retrieveCspSignaturePrivateKey() throws Xdi2ClientException, GeneralSecurityException {

	   logger.debug("retrieveCspSignaturePrivateKey");
		XDIDiscoveryResult xdiDiscoveryResult = this.getXdiDiscoveryClient().discoverFromRegistry(this.getCspCloudNumber().getXDIAddress());
		URI cspXdiEndpoint = xdiDiscoveryResult.getXdiEndpointUri();

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
		if(cspSecretToken != null && !cspSecretToken.isEmpty() && (this.cspSignaturePrivateKey == null || this.cspSignaturePrivateKey.toString().isEmpty() ))
		{
		   try
         {
            this.retrieveCspSignaturePrivateKey();
         } catch (Xdi2ClientException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } catch (GeneralSecurityException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
		}
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
	public XDIAddress getRnCspLinkContract() {

		return this.rnCspLinkContract;
	}

	public void setRnCspLinkContract(XDIAddress rnCspLinkContract) {

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
