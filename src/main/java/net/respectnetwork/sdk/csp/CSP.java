package net.respectnetwork.sdk.csp;

import java.util.Date;
import java.util.Map;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;
import xdi2.core.xri3.XDI3Segment;

/**
 * This interface represents CSP-related functionality of this SDK to communicate with
 * - the Respect Network Registration Service (RN)
 * - a CSP Environment (CSP)
 * - a User Cloud
 * 
 * The comments on individual methods reference the following diagrams:
 * [A] Initial Sign-Up: https://wiki.respectnetwork.net/wiki/Alice_Signs_Up
 * [B] Inline Provisioning: https://wiki.respectnetwork.net/wiki/Inline_Provisioning
 */
public interface CSP {

	/*
	 * Methods for registering User Clouds
	 */

	/**
	 * This method registers a new User Cloud in the CSP Cloud.
	 * In addition:
	 *   - this registers the User Cloud's XDI endpoint in the CSP Cloud.
	 *   - this optionally registers the User Cloud's secret token in the CSP Cloud.
	 * Used in:
	 *   [A] 2.1.1.2 
	 *   [B] 3.1.1.2
	 */
	public void registerCloudInCSP(CloudNumber cloudNumber, String secretToken) throws Xdi2ClientException;

	/*
	 * Methods for registering Cloud Names
	 */

	/**
	 * This method checks if a Cloud Name has been registered in the RN Registration Service.
	 * @return A Cloud Number if the Cloud Name has been registered, otherwise null.
	 * Used in:
	 *   [A] Not used
	 *   [B] Not used
	 */
	public CloudNumber checkCloudNameAvailableInRN(CloudName cloudName) throws Xdi2ClientException;

	/**
	 * This method checks if a verified phone number and verified e-mail address have been registered in the RN Member Graph Service.
	 * @return An array of exactly two Cloud Numbers that have been registered for the phone number and the e-mail address respectively.
	 * Each one of the two Cloud Numbers may be null, if the phone number or e-mail address have not been registered.
	 * Used in:
	 *   [A] 3.1.1.5.1
	 *   [B] 4.1.1.5.1
	 */
	public CloudNumber[] checkPhoneAndEmailAvailableInRN(String verifiedPhone, String verifiedEmail) throws Xdi2ClientException;

	/**
	 * This method registers a new Cloud Name and Cloud Number in the RN Registration Service and
	 * RN Member Graph Service.
	 * In addition:
	 *   - this registers the User Cloud's XDI endpoint in the RN Registration Service and RN Member Graph Service.
	 *   - this optionally registers a verified phone number and verified e-mail address in the RN Member Graph Service.
	 * Used in:
	 *   [A] 5.1.1.3
	 *   [B] Not used
	 */
	public void registerCloudNameInRN(CloudName cloudName, CloudNumber cloudNumber, String verifiedPhone, String verifiedEmail) throws Xdi2ClientException;

	/**
	 * This method registers a new Cloud Name and Cloud Number in the CSP Cloud.
	 * Used in:
	 *   [A] 5.1.1.7
	 *   [B] Not used
	 */
	public void registerCloudNameInCSP(CloudName cloudName, CloudNumber cloudNumber) throws Xdi2ClientException;

	/**
	 * This method registers a new Cloud Name and Cloud Number in the User Cloud.
	 * Used in:
	 *   [A] 5.1.1.5
	 *   [B] Not used
	 */
	public void registerCloudNameInCloud(CloudName cloudName, CloudNumber cloudNumber, String secretToken) throws Xdi2ClientException;

	/*
	 * Methods for updating and querying RN information for existing Cloud Numbers and Clouds.
	 */

	/**
	 * This method registers or updates a User Cloud's XDI endpoint in the RN Registration Service and RN Member Graph Service.
	 * Normally, it is not necessary to call this, since it is automatically done by the registerCloudNameInRN() method.
	 * Used in:
	 *   [A] Not used
	 *   [B] 5.1.3.3
	 */
	public void setCloudXdiEndpointInRN(CloudNumber cloudNumber, String cloudXdiEndpoint) throws Xdi2ClientException;

	/**
	 * This methods registers or updates a verified phone number and verified e-mail address in the RN Member Graph Service.
	 * Normally, it is not necessary to call this, since it is automatically done by the registerCloudNameInRN() method.
	 * Used in:
	 *   [A] Not used
	 *   [B] 5.1.3.3
	 */
	public void setPhoneAndEmailInRN(CloudNumber cloudNumber, String verifiedPhone, String verifiedEmail) throws Xdi2ClientException;

	/**
	 * This method sets a flag for a Cloud Number to indicate that it is a member of
	 * the Respect Network.
	 * Used in:
	 *   [A] Not used
	 *   [B] Not used
	 */
	public void setRespectNetworkMembershipInRN(CloudNumber cloudNumber)  throws Xdi2ClientException;

	/**
	 * This method retrieves a flag for a Cloud Number to indicate whether it is a member of
	 * the Respect Network.
	 * Used in:
	 *   [A] Not used
	 *   [B] Not used
	 */
	public boolean checkRespectNetworkMembershipInRN(CloudNumber cloudNumber) throws Xdi2ClientException;

	/**
	 * This method sets a flag for a Cloud Number to indicate that it is a member of
	 * the Respect First program.
	 * Used in:
	 *   [A] Not used
	 *   [B] Not used
	 */
	public void setRespectFirstMembershipInRN(CloudNumber cloudNumber, Date expirationTime)  throws Xdi2ClientException;

	/**
	 * This method retrieves a flag for a Cloud Number to indicate whether it is a member of
	 * the Respect First program.
	 * Used in:
	 *   [A] Not used
	 *   [B] Not used
	 */
	public boolean checkRespectFirstMembershipInRN(CloudNumber cloudNumber) throws Xdi2ClientException;

	/**
	 * This method sets a flag for a Cloud Number to indicate that it is a member of
	 * the Respect First Lifetime program.
	 * Used in:
	 *   [A] Not used
	 *   [B] Not used
	 */
	public void setRespectFirstLifetimeMembershipInRN(CloudNumber cloudNumber)  throws Xdi2ClientException;

	/**
	 * This method retrieves a flag for a Cloud Number to indicate whether it is a member of
	 * the Respect First Lifetime program.
	 * Used in:
	 *   [A] Not used
	 *   [B] Not used
	 */
	public boolean checkRespectFirstLifetimeMembershipInRN(CloudNumber cloudNumber) throws Xdi2ClientException;

	/*
	 * Methods for updating and querying CSP information for existing Cloud Numbers and Clouds.
	 */

	/**
	 * This method registers or updates a User Cloud's XDI endpoint in the CSP Cloud.
	 * Normally, it is not necessary to call this, since it is automatically done by the registerCloudInCSP() method.
	 * Used in:
	 *   [A] Not used
	 *   [B] Not used
	 */
	public void setCloudXdiEndpointInCSP(CloudNumber cloudNumber, String cloudXdiEndpoint) throws Xdi2ClientException;

	/**
	 * This method registers or updates a User Cloud's secret token in the CSP Cloud.
	 * Normally, it is not necessary to call this, since it is automatically done by the registerCloudInCSP() method.
	 * Used in:
	 *   [A] 5.1.1.1
	 *   [B] Not used
	 */
	public void setCloudSecretTokenInCSP(CloudNumber cloudNumber, String secretToken) throws Xdi2ClientException;

	/**
	 * This method checks what Cloud Names exist for a given Cloud Number.
	 * Used in:
	 *   [A] Not used
	 *   [B] Not used
	 */
	public CloudName[] checkCloudNamesInCSP(CloudNumber cloudNumber) throws Xdi2ClientException;

	/*
	 * Methods for updating Cloud information for existing Cloud Numbers and Clouds.
	 */

	/**
	 * This method checks if a given secret token is correct for the User Cloud.
	 * Used in:
	 *   [A] Not used
	 *   [B] Not used
	 */
	public void authenticateInCloud(CloudNumber cloudNumber, String secretToken) throws Xdi2ClientException;

	/**
	 * This method registers or updates additional discoverable services in a User Cloud.
	 * Used in:
	 *   [A] 2.1.1.4
	 *   [B] 3.1.1.4
	 */
	public void setCloudServicesInCloud(CloudNumber cloudNumber, String secretToken, Map<XDI3Segment, String> services) throws Xdi2ClientException;

	/**
	 * This method registers or updates a verified phone number and verified e-mail address in the User's Cloud.
	 * Used in:
	 *   [A] 3.1.1.3
	 *   [B] 4.1.1.2
	 */
	public void setPhoneAndEmailInCloud(CloudNumber cloudNumber, String secretToken, String verifiedPhone, String verifiedEmail) throws Xdi2ClientException;
}
