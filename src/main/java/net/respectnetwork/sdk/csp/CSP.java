package net.respectnetwork.sdk.csp;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.xri3.XDI3Segment;

/**
 * This interface represents CSP-related functionality of this SDK to communicate with
 * - the Respect Network Registration Service 
 * - a Cloud Hosting Environment
 */
public interface CSP {

	public CloudNameRegistration checkCloudNameAvailable(XDI3Segment cloudName) throws Xdi2ClientException;
	public CloudNameRegistration registerCloudName(XDI3Segment cloudName) throws Xdi2ClientException;
	public CloudRegistration registerCloud(CloudNameRegistration cloudNameRegistration, String secretToken) throws Xdi2ClientException;
	public void setCloudXdiUrl(CloudNameRegistration cloudNameRegistration, CloudRegistration cloudRegistration) throws Xdi2ClientException;
	public void setCloudSecretToken(CloudNameRegistration cloudNameRegistration, String secretToken) throws Xdi2ClientException;
}
