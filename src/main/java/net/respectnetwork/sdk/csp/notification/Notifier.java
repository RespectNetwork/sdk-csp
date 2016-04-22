package net.respectnetwork.sdk.csp.notification;

import java.util.Map;

public interface Notifier {
    
    /**
     * 
     * @param phoneNumber
     * @param Message
     * @throws NotificationException
     */
    public void sendSMSNotification(String phoneNumber, String Message) throws NotificationException;
    
    /**
     * 
     * @param email
     * @param Message
     * @throws NotificationException
     */
    public void sendEmailNotification(String email, String Message, String subject) throws NotificationException;
    
    /**
     * Method to send email notification.
     * @param toEmail Message recipient type TO.
     * @param bccEmail Message recipient type BCC.
     * @param Message Email content.
     * @throws NotificationException
     */
    public void sendEmailNotification(String toEmail, String bccEmail, String message, String subject) throws NotificationException;

    /**
     * @param event
     * @param emailAddress
     * @param cspCloudName
     * @param placeHolders
     * @throws NotificationException
     */
    public void sendEmailNotification(String event, String emailAddress,
	String cspCloudName, Map<String, Object> placeHolders, String subject) throws NotificationException;

}
