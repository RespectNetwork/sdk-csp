package net.respectnetwork.sdk.csp.notification;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import org.apache.commons.lang.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.respectnetwork.sdk.csp.notification.MessageCreationException;

public class BasicMessageManager implements MessageManager {
    
    
    /** Class Logger */
    private static final Logger logger = LoggerFactory
            .getLogger(BasicMessageManager.class);
    

    public BasicMessageManager() {
        // TODO Auto-generated constructor stub
    }

    /** Email Template */
    private String emailTemplate;
    
    /** SMS Template */
    private String smsTemplate;

    /**
     * @return the emailTemplate
     */
    public String getEmailTemplate() {
        return emailTemplate;
    }

    /**
     * @param emailTemplate the emailTemplate to set
     */
    public void setEmailTemplate(String emailTemplate) {
        this.emailTemplate = emailTemplate;
    }

    /**
     * @return the smsTemplate
     */
    public String getSmsTemplate() {
        return smsTemplate;
    }

    /**
     * @param smsTemplate the smsTemplate to set
     */
    public void setSmsTemplate(String smsTemplate) {
        this.smsTemplate = smsTemplate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createEmailMessage(String validationToken,
            String validationEndpoint, String cloudNumber) throws MessageCreationException {
        
        try {
            
            HashMap<String, String> valuesMap = new HashMap<String, String>();
            valuesMap.put("validationToken", validationToken);
            String validationURL = validationEndpoint + "?cloudNumber=" + URLEncoder.encode(cloudNumber, "UTF-8");  
            valuesMap.put("validationURL", validationURL);
                  
            StrSubstitutor sub = new StrSubstitutor(valuesMap);
            sub.setVariablePrefix("!!");
            sub.setVariableSuffix("!!");
            String resolvedString = sub.replace(emailTemplate);
            
            return resolvedString;

        
        } catch (UnsupportedEncodingException e) {
            String error = "Problem creating Email Message: " + e.getMessage();
            logger.debug(error);
            throw new MessageCreationException(error);
        }
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createSMSMessage(String validationToken)
            throws MessageCreationException {

        HashMap<String, String> valuesMap = new HashMap<String, String>();
        valuesMap.put("validationToken", validationToken);        
        
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        sub.setVariablePrefix("!!");
        sub.setVariableSuffix("!!");
        String resolvedString = sub.replace(smsTemplate);
        
        return resolvedString;
    }

}
