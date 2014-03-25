package net.respectnetwork.sdk.csp.notification;

import net.respectnetwork.sdk.csp.notification.MessageCreationException;

public interface MessageManager {
    
    public  String createEmailMessage(String validationToken, String validationEndpoint, String clouldNumber) throws MessageCreationException;
    public  String createSMSMessage(String validationToken) throws MessageCreationException;


}
