package net.respectnetwork.sdk.csp;

import net.respectnetwork.sdk.csp.notification.MessageCreationException;
import net.respectnetwork.sdk.csp.notification.MessageManager;
import net.respectnetwork.sdk.csp.notification.NotificationException;
import net.respectnetwork.sdk.csp.notification.Notifier;
import net.respectnetwork.sdk.csp.notification.TokenException;
import net.respectnetwork.sdk.csp.notification.TokenKey;
import net.respectnetwork.sdk.csp.notification.TokenManager;
import net.respectnetwork.sdk.csp.validation.CSPValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicUserValidator implements UserValidator {
    
    /** Class Logger */
    private static final Logger log = LoggerFactory.getLogger(BasicUserValidator.class);
    
    
    /** Notification Service */
    private Notifier theNotifier;
    
    /** Token Manager */
    private TokenManager tokenManager;
    
    /** Message Manager */
    private MessageManager messageManager;
    
    /** Registration Codes Validation Endpoint */
    private String validationEndpoint;
    
    /**
     * @return the theNotifier
     */
    public Notifier getTheNotifier() {
        return theNotifier;
    }


    /**
     * @param theNotifier the theNotifier to set
     */
    public void setTheNotifier(Notifier theNotifier) {
        this.theNotifier = theNotifier;
    }


    /**
     * @return the tokenManager
     */
    public TokenManager getTokenManager() {
        return tokenManager;
    }


    /**
     * @param tokenManager the tokenManager to set
     */
    public void setTokenManager(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }


    /**
     * @return the messageManager
     */
    public MessageManager getMessageManager() {
        return messageManager;
    }


    /**
     * @param messageManager the messageManager to set
     */
    public void setMessageManager(MessageManager messageManager) {
        this.messageManager = messageManager;
    }


    /**
     * @return the validationEndpoint
     */
    public String getValidationEndpoint() {
        return validationEndpoint;
    }


    /**
     * @param validationEndpoint the validationEndpoint to set
     */
    public void setValidationEndpoint(String validationEndpoint) {
        this.validationEndpoint = validationEndpoint;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void sendValidationMessages(String sessionKey, String email, String mobilePhone, String cspName)
            throws CSPValidationException {
            
        try {
            if (validationEndpoint == null || tokenManager == null || theNotifier == null ) {
                throw new CSPValidationException("Basic CSP not properly configured,"
                        + " check that all required properties are set.");
            }
            
            TokenKey emailTokenKey = new TokenKey(sessionKey, "EMAIL");
            TokenKey smsTokenKey = new TokenKey(sessionKey, "SMS");
            
            tokenManager.inValidateToken(emailTokenKey);
            tokenManager.inValidateToken(smsTokenKey);            
            
            if(!"".equals(email) && email != null) {
                String emailValidationCode = tokenManager.createToken(emailTokenKey);
                String emailMessage = messageManager.createEmailMessage(emailValidationCode, validationEndpoint, sessionKey, cspName);
                theNotifier.sendEmailNotification(email, emailMessage);
            }
            String smsValidationCode = tokenManager.createToken(smsTokenKey);
            String smsMessage = messageManager.createSMSMessage(smsValidationCode, cspName);
            theNotifier.sendSMSNotification(mobilePhone, smsMessage);

            // done
            log.debug("Send ValidationMessages to {} and {}", email, mobilePhone);

        } catch (NotificationException e) {
            log.warn("Problem Notifying User: {}", e.getMessage());
            throw new CSPValidationException(e.getMessage());            
        } catch (TokenException e) {
            log.warn("Problem Generating Verification Token for User: {}", e.getMessage());
            throw new CSPValidationException(e.getMessage());        
        } catch (MessageCreationException e) {
            log.warn("Problem Creating Notification Message: {}", e.getMessage());
            throw new CSPValidationException(e.getMessage());
        }
    }
     

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateCodes(String sessionIdentifier, String emailCode, String smsCode) throws CSPValidationException {
        
        try {
            boolean result = false;
            
            TokenKey emailTokenKey = new TokenKey(sessionIdentifier, "EMAIL");
            TokenKey smsTokenKey = new TokenKey(sessionIdentifier, "SMS");

            if(emailCode != null && !emailCode.equals("")){
                  result = (tokenManager.validateToken(emailTokenKey, emailCode) &&
                tokenManager.validateToken(smsTokenKey, smsCode));
              } else {
                  result = tokenManager.validateToken(smsTokenKey, smsCode);
              }
            
            //If the codes are used for verification once  they should then be invalidated.
            if (result) {
                tokenManager.inValidateToken(emailTokenKey);
                tokenManager.inValidateToken(smsTokenKey);
            }
           
            return result;
            
        } catch (TokenException e) {
            String error = "Error validating token: {}" + e.getMessage();
            log.debug(error);
            throw new CSPValidationException(error);
        } 
    }

}
