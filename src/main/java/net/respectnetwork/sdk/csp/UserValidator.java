package net.respectnetwork.sdk.csp;

import net.respectnetwork.sdk.csp.validation.CSPValidationException;

public interface UserValidator {



    /**
     * Create User Graph and Validate User Information
     * 
     * 1) Send out Verification Contacts: SMS + eMail
     *  
     * Used in:
     *  [A] 3.1.1
     *  [B] Not used
     *
     * @param sessionKey 
     * @param email
     * @param mobilePhone 
     * @throws CSPValidationException
     */
    public void sendValidationMessages(String sessionKey, String email, String mobilePhone, String cspName)
            throws CSPValidationException; 
    
    /**
     * Validate Codes for both email and SMS as part of the registration process
     * 
     * Used in:
     *  [A] 4.1.1
     *  [B] Not used
     *      
     * @param sessionIdentifier  created for new user. Key for Lookup.
     * @param emailCode validationCode e-mailed to User
     * @param smsCode validation code SMSed to  user
     * @throws CSPValidationException
     * @return whether or not Validation succeeded
     * 
     */
    public boolean validateCodes(String sessionIdentifier, String emailCode, String smsCode, String email, String phone)
            throws CSPValidationException;
    
   

    
}
