package net.respectnetwork.sdk.csp;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;

/**
 * This interface represents CSP-related functionality of this SDK to communicate with
 * - the Respect Network Registration Service (RN)
 * - a Cloud Hosting Environment (HE)
 */
public interface CSP {

	public void registerCloudInCSP(CloudNumber cloudNumber, String secretToken) throws Xdi2ClientException;

	public CloudNumber checkCloudNameAvailableInRN(CloudName cloudName) throws Xdi2ClientException;
	public void registerCloudNameInRN(CloudName cloudName, CloudNumber cloudNumber) throws Xdi2ClientException;
	public CloudNumber registerCloudNameInRN(CloudName cloudName) throws Xdi2ClientException;

	public void setCloudVerifiedContactInformationInRN(CloudNumber cloudNumber, String email, String phone);

	public void setCloudXdiEndpointInRN(CloudNumber cloudNumber, String cloudXdiEndpoint) throws Xdi2ClientException;
	public void setCloudXdiEndpointInCSP(CloudNumber cloudNumber, String cloudXdiEndpoint) throws Xdi2ClientException;
	public void setCloudSecretTokenInCSP(CloudNumber cloudNumber, String secretToken) throws Xdi2ClientException;
}
