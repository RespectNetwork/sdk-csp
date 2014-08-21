package net.respectnetwork.sdk.csp;

import java.security.PrivateKey;
import java.util.Date;
import java.util.Map;

import net.respectnetwork.sdk.csp.discount.NeustarRNCampaignCode;
import net.respectnetwork.sdk.csp.discount.NeustarRNDiscountCode;
import net.respectnetwork.sdk.csp.discount.RespectNetworkRNDiscountCode;
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
 * [C] Dependency Flow: http://docs.respectnetwork.net/wiki/Dependent_Registration
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
	 * This method checks if a Cloud Name is available in the RN Registration Service.
	 * @return True, if the Cloud Name is available, false otherwise.
	 * Used in:
	 *   [A] Not used
	 *   [B] Not used
	 */
	public boolean checkCloudNameAvailableInRN(CloudName cloudName) throws Xdi2ClientException;

	/**
	 * This method checks if a Cloud Name has been registered in the RN Registration Service.
	 * @return The Cloud Number if the Cloud Name has been registered, false otherwise.
	 * Used in:
	 *   [A] Not used
	 *   [B] Not used
	 */
	public CloudNumber checkCloudNameInRN(CloudName cloudName) throws Xdi2ClientException;

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
	public void registerCloudNameInRN(CloudName cloudName, CloudNumber cloudNumber, String verifiedPhone, String verifiedEmail, NeustarRNDiscountCode neustarRNDiscountCode) throws Xdi2ClientException;

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

	/**
	 * This method deletes a Cloud Number from the RN Registration Service and
	 * RN Member Graph Service.
	 * Used in:
	 *   [A] Not used
	 *   [B] Not used
	 */
	public void deleteCloudNameInRN(CloudName cloudName, CloudNumber cloudNumber) throws Xdi2ClientException;

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
	 * This method sets a flag for a Cloud Number in the RN Member Graph Service to indicate that it is a member of
	 * the Respect Network.
	 * Used in:
	 *   [A] Not used
	 *   [B] Not used
	 */
	public void setRespectNetworkMembershipInRN(CloudNumber cloudNumber, Date expirationTime, RespectNetworkRNDiscountCode respectNetworkRNDiscountCode)  throws Xdi2ClientException;

	/**
	 * This method retrieves a flag for a Cloud Number in the RN Member Graph Service to indicate whether it is a member of
	 * the Respect Network.
	 * Used in:
	 *   [A] Not used
	 *   [B] Not used
	 */
	public boolean checkRespectNetworkMembershipInRN(CloudNumber cloudNumber) throws Xdi2ClientException;

	/**
	 * This method sets a flag for a Cloud Number in the RN Member Graph Service to indicate that it is a member of
	 * the Respect First program.
	 * Used in:
	 *   [A] Not used
	 *   [B] Not used
	 */
	public void setRespectFirstMembershipInRN(CloudNumber cloudNumber)  throws Xdi2ClientException;

	/**
	 * This method retrieves a flag for a Cloud Number in the RN Member Graph Service to indicate whether it is a member of
	 * the Respect First program.
	 * Used in:
	 *   [A] Not used
	 *   [B] Not used
	 */
	public boolean checkRespectFirstMembershipInRN(CloudNumber cloudNumber) throws Xdi2ClientException;

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
	 * This method checks what Cloud Names exist for a given Cloud Number in the CSP Cloud.
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
	 * Management Utility for setting CSP Service EndPoints in the CSP  Cloud
     */
	public void setCloudServicesForCSPInCSP(CloudNumber cloudNumber, String secretToken, String cspXdiEndpoint, Map<XDI3Segment, String> services) throws Xdi2ClientException;

	/**
	 * This method registers or updates a verified phone number and verified e-mail address in the User's Cloud.
	 * Used in:
	 *   [A] 3.1.1.3
	 *   [B] 4.1.1.2
	 */
	public void setPhoneAndEmailInCloud(CloudNumber cloudNumber, String secretToken, String verifiedPhone, String verifiedEmail) throws Xdi2ClientException;
	
    /**
     * Get the Number of Respect First Members in the Member Graph Service.
     * Used in:
     *   [A] Not used
     *   [B] Not used
     */
    public long getRespectFirstMemberCount() throws Xdi2ClientException;
    
    /** 
     * Get CSP Information
     * 
     *  Used in:
     *   [A] Utility Method
     *   [B] Utility Method
     */    
    public CSPInformation getCspInformation();
    
    /** 
     * 
     * Set CSP Information
     * 
     *  Used in:
     *   [A] Utility Method
     *   [B] Utility Method
     */   
    public void setCspInformation(CSPInformation cspInformation);
    
    

    /**
     * Set Guardian -> Dependent Relationship in User's Cloud
     * 
     *  Used in:
     *   [C] Dependency Flow.  
     */
    public void setGuardianshipInCloud(CSPInformation cspInformation, CloudNumber guardian,
        CloudNumber dependent, Date dependentBirthDate, boolean withConsent, String secretToken,
        PrivateKey guardianPrivateSigningKey , String dependentToken)
            throws Xdi2ClientException;   
      
      
    /**
     * Set Guardian -> Dependent Relationship in CSP Cloud
     * 
     *  Used in:
     *   [C] Dependency Flow.  
     */
    public void setGuardianshipInCSP(CSPInformation cspInformation, CloudNumber guardian,
        CloudNumber dependent, Date dependentBirthDate, boolean withConsent,
        PrivateKey guardianPrivateSigningKey)
            throws Xdi2ClientException; 
       
    
    /**
     * Set Guardian -> Dependent Relationship in Respect Network
     * 
     *  Used in:
     *   [C] Dependency Flow.  
     */
    public void setGuardianshipInRN(CSPInformation cspInformation, CloudNumber guardian,
        CloudNumber dependent, Date dependentBirthDate, boolean withConsent,
        PrivateKey guardianPrivateSigningKey)
            throws Xdi2ClientException; 
    
    
    /**
     *  Get a Guardian's Dependents from the CSP Graph 
     * 
     *  Used in:
     *   [C] Dependency Flow.  
     */
    public CloudNumber[] getMyDependentsInCSP(CSPInformation cspInformation, CloudNumber guardian)
        throws Xdi2ClientException;
    
    
    /**
     *  Get a Dependent's Parent from the CSP Graph 
     * 
     *  Used in:
     *   [C] Dependency Flow.  
     */
    public CloudNumber[] getMyGuardiansInCSP(CSPInformation cspInformation, CloudNumber dependent)
        throws Xdi2ClientException;
    
    
	/*
	 * Methods for adding additional CloudNames
	 */
          
	/**
	 * Add an additional Cloud Name to an Existing Respect Network Account.
	 * The CloudNumber has to exist as an existing RespectNetwork Member.
	 * The Discount Code Provided has to be NeustarRNDiscountCode.FirstMillion
	 * The Campaign Code has to be NeustarRNCampaignCode.FirstFiveNames
	 * It is up to the calling CSP to track the number of Names associated
	 * with a Cloud Number and to limit existing Names to five.
	 * 
	 * @param cloudName previously unRegistered ClouldName
	 * @param cloudNumber existing RN Member Cloud Number registered by the calling registrar.
	 * @param neustarRNDiscountCode 
	 * @param neustarRNCampaignCode
	 * @throws Xdi2ClientException
	 */
	public void registerAdditionalCloudNameInRN(CloudName cloudName, CloudNumber cloudNumber,
			NeustarRNDiscountCode neustarRNDiscountCode, NeustarRNCampaignCode neustarRNCampaignCode)
					throws Xdi2ClientException;
	
	/**
	 * Register an additional of New CloudName to Existing CloudNumber in CSP Graph
	 * 
	 * @param cloudName previously unRegistered ClouldName
	 * @param cloudNumber existing RN Member Cloud Number registered by the calling registrar.
	 * @throws Xdi2ClientException
	 */
	public void registerAdditionalCloudNameInCSP(CloudName cloudName, CloudNumber cloudNumber)
			throws Xdi2ClientException;
	
	
	/**
	 * Register Addition of New CloudName to Existing CloudNumber in User Graph
	 * 
	 * @param cloudName  previously unRegistered ClouldName
	 * @param cloudNumber existing RN Member Cloud Number registered by the calling registrar.
	 * @param userSecretToken secret token for user to allow updates to their graph.
	 * @throws Xdi2ClientException
	 */
	public void registerAdditionalCloudNameInCloud(CloudName cloudName, CloudNumber cloudNumber,
			String userSecretToken) throws Xdi2ClientException;
	
	
	/*
	 * Methods for Adding/Registering Business Name
	 */
	
	/**
     * Register a Business Cloud Name associated with a CloudNumber
     * 
	 * @param businessCloudName  the name that should be registered on the authority
	 * @param businessCloudNumber the authority that should be created or added to
	 * @param contactCloudNumber the billing contact for the business cloud
	 * @throws Xdi2ClientException
	 */
	public void registerBusinessNameInRN(CloudName businessCloudName, CloudNumber businessCloudNumber,
			CloudNumber contactCloudNumber) throws Xdi2ClientException;
	
	
	/**
     * Register a Business Cloud Name associated with a CloudNumber in the CSP Graph
     * 
	 * @param businessCloudName  the name that should be registered on the authority
	 * @param businessCloudNumber the authority that should be created or added to
 	 * @param contactCloudNumber the billing contact for the business cloud
	 * @throws Xdi2ClientException
	 */
	public void registerBusinessNameInCSP(CloudName businessCloudName, CloudNumber businessCloudNumber,
			CloudNumber contactCloudNumber) throws Xdi2ClientException;
	
	
	/**
     * Register a Business Cloud Name associated with a CloudNumber in the User Graph
     * 
	 * @param businessCloudName  the name that should be registered on the authority
	 * @param businessCloudNumber the authority that should be created or added to
 	 * @param contactCloudNumber the billing contact for the business cloud
	 * @throws Xdi2ClientException
	 */
	public void registerBusinessNameInCloud(CloudName businessCloudName, CloudNumber businessCloudNumber,
			CloudNumber contactCloudNumber) throws Xdi2ClientException;
	
	/**
	 * updates phone number for a given cloud number with new phone number
	 * @param cloudNumber
	 * @param verifiedPhone
	 * @param oldVerifiedPhone
	 * @throws Xdi2ClientException
	 */
	public void updatePhoneInRN(CloudNumber cloudNumber, String verifiedPhone, String oldVerifiedPhone ) throws Xdi2ClientException;	
	
	/**
	 * updates email for a given cloud number with new email
	 * @param cloudNumber
	 * @param verifiedEmail
	 * @param oldVerifiedEmail
	 * @throws Xdi2ClientException
	 */
	public void updateEmailInRN(CloudNumber cloudNumber, String verifiedEmail, String oldVerifiedEmail ) throws Xdi2ClientException;	
	
	/**
	 * retrieves csp cloudNumber for a member
	 * @param cloudNumber
	 * @return
	 * @throws Xdi2ClientException
	 */
	public CloudNumber getMemberRegistrar(CloudNumber cloudNumber) throws Xdi2ClientException;

	/**
	 * deletes cloud in CSP
	 * @param cloudNumber
	 * @param secretToken
	 * @throws Xdi2ClientException
	 */
	public void deleteCloudInCSP(CloudNumber cloudNumber, String secretToken) throws Xdi2ClientException;

	/**
	 * transfers users cloud to a different csp
	 * @param cloudNumber
	 * @param cloudNames
	 * @param secretToken
	 * @throws Xdi2ClientException
	 */
	public void transferCloudInCSP(CloudNumber cloudNumber, CloudName[] cloudNames, String secretToken) throws Xdi2ClientException;

	/**
	 * change member registrar for a given cloud number, deletes current member registrar
	 * @param cloudNumber
	 * @throws Xdi2ClientException
	 */
	public void changeMemberRegistrarInRN(CloudNumber cloudNumber) throws Xdi2ClientException;
    
}
