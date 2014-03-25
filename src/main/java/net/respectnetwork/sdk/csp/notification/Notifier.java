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


}
