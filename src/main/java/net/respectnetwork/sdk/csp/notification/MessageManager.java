package net.respectnetwork.sdk.csp.notification;

import net.respectnetwork.sdk.csp.notification.MessageCreationException;

public interface MessageManager {
    
    public  String createEmailMessage(String validationToken, String validationEndpoint, String clouldNumber, String cspName) throws MessageCreationException;
    public  String createSMSMessage(String validationToken, String cspName) throws MessageCreationException;


}
