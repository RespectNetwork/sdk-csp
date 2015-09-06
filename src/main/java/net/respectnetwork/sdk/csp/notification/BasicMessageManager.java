package net.respectnetwork.sdk.csp.notification;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicMessageManager implements MessageManager {

    /** Class Logger */
    private static final Logger logger = LoggerFactory.getLogger(BasicMessageManager.class);

    public BasicMessageManager() {

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
     * @param emailTemplate
     *            the emailTemplate to set
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
     * @param smsTemplate
     *            the smsTemplate to set
     */
    public void setSmsTemplate(String smsTemplate) {
        this.smsTemplate = smsTemplate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createEmailMessage(String validationToken, String validationEndpoint, String cloudNumber, String cspName)
            throws MessageCreationException {

        try {

            HashMap<String, String> valuesMap = new HashMap<String, String>();
            valuesMap.put("validationToken", validationToken);
            String validationURL = validationEndpoint + "?cloudNumber=" + URLEncoder.encode(cloudNumber, "UTF-8");
            valuesMap.put("validationURL", validationURL);

            StrSubstitutor sub = new StrSubstitutor(valuesMap);
            sub.setVariablePrefix("!!");
            sub.setVariableSuffix("!!");
            String resolvedString = sub.replace(getTemplate(cspName +".email.template"));

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
    public String createSMSMessage(String validationToken, String cspName) throws MessageCreationException {

        HashMap<String, String> valuesMap = new HashMap<String, String>();
        valuesMap.put("validationToken", validationToken);

        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        sub.setVariablePrefix("!!");
        sub.setVariableSuffix("!!");
        String resolvedString = sub.replace(getTemplate(cspName + ".sms.template"));

        return resolvedString;
    }

    public String getTemplate(String key) {
        Properties properties = new Properties();
        String fileName = "notification.properties";
        HashMap<String, String> keyValue = new HashMap<String, String>();
        if (keyValue.isEmpty()) {
            try {
                properties.load(BasicMessageManager.class.getClassLoader().getResourceAsStream(fileName));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!properties.isEmpty()) {
                for (final Entry<Object, Object> entry : properties.entrySet()) {
                    keyValue.put((String) entry.getKey(), (String) entry.getValue());
                }
            }
        }
        return keyValue.get(key);
    }
}
