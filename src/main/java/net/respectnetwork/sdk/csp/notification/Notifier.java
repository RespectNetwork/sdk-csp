package net.respectnetwork.sdk.csp.notification;

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
    public void sendEmailNotification(String email, String Message) throws NotificationException;
    
    /**
     * Method to send email notification.
     * @param toEmail Message recipient type TO.
     * @param bccEmail Message recipient type BCC.
     * @param Message Email content.
     * @throws NotificationException
     */
    public void sendEmailNotification(String toEmail, String bccEmail, String message) throws NotificationException;

}
