package net.respectnetwork.sdk.csp;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;

/**
 * This interface represents CSP-related functionality of this SDK to communicate with
 * - the Respect Network Registration Service 
 * - a Cloud Hosting Environment
 */
public interface CSP {

	public void registerCloud(CloudNumber cloudNumber, String secretToken) throws Xdi2ClientException;
	public CloudNumber checkCloudNameAvailable(CloudName cloudName) throws Xdi2ClientException;
	public void registerCloudName(CloudName cloudName, CloudNumber cloudNumber) throws Xdi2ClientException;
	public CloudNumber registerCloudName(CloudName cloudName) throws Xdi2ClientException;

	public void setCloudXdiEndpoint(CloudNumber cloudNumber) throws Xdi2ClientException;
	public void setCloudXdiEndpoint(CloudNumber cloudNumber, String cloudXdiEndpoint) throws Xdi2ClientException;
	public void setCloudSecretToken(CloudNumber cloudNumber, String secretToken) throws Xdi2ClientException;
}
