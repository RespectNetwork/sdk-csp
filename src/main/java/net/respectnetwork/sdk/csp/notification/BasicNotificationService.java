package net.respectnetwork.sdk.csp.notification;


import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.SmsFactory;
import com.twilio.sdk.resource.instance.Sms;

/**
 * Basic Notification Service uses
 *
 *    1) Twillio for SMS Notifications
 *    2) Java Mail Transport for EMail
 *
 */
public class BasicNotificationService implements Notifier {
    
    /** Class Logger */
    private static final Logger LOG = LoggerFactory
            .getLogger(BasicNotificationService.class);
    
    /** Twillio Account SID */
    private String twillioAccountSID;
    
    /** Twillio Authentication Token */
    private String twillioAuthToken;
    
    /** Max Size of SMS Message */
    private static final int MAX_SMS_SIZE = 100;
    
    /** CSP  Phone Number for sending SMS Messages */
    private  String cspSMSNumber;
    
    /** Verification Email Subject */
    private String emailSubject;
    
    /** Who the Verification email is from */
    private String emailFrom;
    
    /** Email Account UserName */
    private  String mailAccountUsername;
    
    /** Email Account Password */
    private  String mailAccountPassword;
    
    /** SMTP  Auth Option */
    private String mailSMTPAuth;
    
    /** SMTP Start TLS Option */
    private String mailStartTLS;
    
    /** SMTP  Host */
    private String mailSMTPHost;
    
    /** SMTP Port */
    private String mailSMTPPort;
    
    /** Debug */
    private String mailDebug;
 
    /** Mail Transport */
    private String mailTransport;
    
    
     
    /**
     * @return the accountSID
     */
    public String getTwillioAccountSID() {
        return twillioAccountSID;
    }

    /**
     * @param accountSID the accountSID to set
     */
    public void setTwillioAccountSID(String accountSID) {
        this.twillioAccountSID = accountSID;
    }

    /**
     * @return the authToken
     */
    public String getAuthToken() {
        return twillioAuthToken;
    }

    /**
     * @param authToken the authToken to set
     */
    public void setAuthToken(String authToken) {
        this.twillioAuthToken = authToken;
    }

    /**
     * @return the twillioAuthToken
     */
    public String getTwillioAuthToken() {
        return twillioAuthToken;
    }

    /**
     * @param twillioAuthToken the twillioAuthToken to set
     */
    public void setTwillioAuthToken(String twillioAuthToken) {
        this.twillioAuthToken = twillioAuthToken;
    }

    /**
     * @return the cspSMSNumber
     */
    public String getCspSMSNumber() {
        return cspSMSNumber;
    }

    /**
     * @param cspSMSNumber the cspSMSNumber to set
     */
    public void setCspSMSNumber(String cspSMSNumber) {
        this.cspSMSNumber = cspSMSNumber;
    }

    /**
     * @return the emailSubject
     */
    public String getEmailSubject() {
        return emailSubject;
    }

    /**
     * @param emailSubject the emailSubject to set
     */
    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    /**
     * @return the emailFrom
     */
    public String getEmailFrom() {
        return emailFrom;
    }

    /**
     * @param emailFrom the emailFrom to set
     */
    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }

    /**
     * @return the mailAccountUsername
     */
    public String getMailAccountUsername() {
        return mailAccountUsername;
    }

    /**
     * @param mailAccountUsername the mailAccountUsername to set
     */
    public void setMailAccountUsername(String mailAccountUsername) {
        this.mailAccountUsername = mailAccountUsername;
    }

    /**
     * @return the mailAccountPassword
     */
    public String getMailAccountPassword() {
        return mailAccountPassword;
    }

    /**
     * @param mailAccountPassword the mailAccountPassword to set
     */
    public void setMailAccountPassword(String mailAccountPassword) {
        this.mailAccountPassword = mailAccountPassword;
    }

    /**
     * @return the mailSMTPAuth
     */
    public String isMailSMTPAuth() {
        return mailSMTPAuth;
    }

    /**
     * @param mailSMTPAuth the mailSMTPAuth to set
     */
    public void setMailSMTPAuth(String mailSMTPAuth) {
        this.mailSMTPAuth = mailSMTPAuth;
    }

    /**
     * @return the mailStartTLS
     */
    public String isMailStartTLS() {
        return mailStartTLS;
    }

    /**
     * @param mailStartTLS the mailStartTLS to set
     */
    public void setMailStartTLS(String mailStartTLS) {
        this.mailStartTLS = mailStartTLS;
    }

    /**
     * @return the mailSMTPHost
     */
    public String getMailSMTPHost() {
        return mailSMTPHost;
    }

    /**
     * @param mailSMTPHost the mailSMTPHost to set
     */
    public void setMailSMTPHost(String mailSMTPHost) {
        this.mailSMTPHost = mailSMTPHost;
    }

    /**
     * @return the mailSMTPPort
     */
    public String getMailSMTPPort() {
        return mailSMTPPort;
    }

    /**
     * @param mailSMTPPort the mailSMTPPort to set
     */
    public void setMailSMTPPort(String mailSMTPPort) {
        this.mailSMTPPort = mailSMTPPort;
    }

    /**
     * @return the mailDebug
     */
    public String getMailDebug() {
        return mailDebug;
    }

    /**
     * @param mailDebug the mailDebug to set
     */
    public void setMailDebug(String mailDebug) {
        this.mailDebug = mailDebug;
    }

    /**
     * @return the mailTransport
     */
    public String getMailTransport() {
        return mailTransport;
    }

    /**
     * @param mailTransport the mailTransport to set
     */
    public void setMailTransport(String mailTransport) {
        this.mailTransport = mailTransport;
    }

    /**
     * Send SMS Notification to Twilio
     */
    @Override
    public void sendSMSNotification(String phoneNumber, String smsMessage)
            throws NotificationException {
         

            TwilioRestClient client = new TwilioRestClient(twillioAccountSID, twillioAuthToken);
         
            // Build a filter for the SmsList
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("Body", checkSMSMessage(smsMessage)));
            params.add(new BasicNameValuePair("To", validatePhoneNumber(phoneNumber)));
            params.add(new BasicNameValuePair("From", cspSMSNumber));
             
             
            SmsFactory smsFactory = client.getAccount().getSmsFactory();
            try {
               Sms sms = smsFactory.create(params);
               LOG.debug("Created SMS message SID: {}",sms.getSid());
            } catch (TwilioRestException e) {
                String smsError = "Problem sending SMS Message: Exception: " +  e.getMessage();
                LOG.warn(smsError);
                throw new NotificationException(smsError);
            }                             
    }

    @Override
    public void sendEmailNotification(String emailTo, String messageOut)
            throws NotificationException {

        Properties props = new Properties();
        
        props.put("mail.smtp.auth", mailSMTPAuth);
        props.put("mail.smtp.starttls.enable", mailStartTLS);
        props.put("mail.smtp.host", mailSMTPHost);
        props.put("mail.smtp.port", mailSMTPPort);
        props.put("mail.debug", mailDebug);
        props.put("mail.transport.protocol", mailTransport);
        
 
        Session session = Session.getInstance(props,
          new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailAccountUsername, mailAccountPassword);
            }
          });
 
        try {
 
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailFrom));
            message.setRecipients(Message.RecipientType.TO,
            InternetAddress.parse(emailTo));
            message.setSubject(emailSubject);
            message.setContent(messageOut, "text/html");
            Transport.send(message);
 
        } catch ( AuthenticationFailedException e) { 
            String errorMsg = "Problem Sending eMail to {} : Issue:Authentication Failed at  mail Service";
            LOG.warn(errorMsg);
            throw new NotificationException(errorMsg);
        } catch (MessagingException e) {
            LOG.warn("Problem Sending eMail to {} : Issue: {}", emailTo, e.getMessage());
            throw new NotificationException(e.getMessage());
        }
    }
        
        
    /** 
     * Basic Message Checker
     * 
     * @param message Message to Check
     * @return sanitized message
     */
    private String checkSMSMessage (String message)
        throws NotificationException{
        
        //First Check length
        if( message.length() >  MAX_SMS_SIZE ) {
            throw new NotificationException ("SMS Message too large");
        } else {
            return message;
        }
    }
    
    /** 
     * Basic Message Checker
     * 
     * @param message Message to Check
     * @return sanitized message
     */
    private String validatePhoneNumber (String number)
        throws NotificationException {
        
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        
        PhoneNumber pNum = new PhoneNumber();
        pNum.setRawInput(number);
        
        boolean isGood = phoneUtil.isValidNumber(pNum);
         
        //Basic Format Check
        if ( isGood ) {
            throw new NotificationException ("Invalid Phone Number Format");
        } else {
            return number;
        }
    }
    
    /**
     * Method to send email notification.
     * @param toEmail Message recipient type TO.
     * @param bccEmail Message recipient type BCC.
     * @param Message Email content.
     * @throws NotificationException
     */
    @Override
    public void sendEmailNotification(String toEmail,
            String bccEmail, String messageOut) throws NotificationException {
        Properties props = new Properties();

        props.put("mail.smtp.auth", mailSMTPAuth);
        props.put("mail.smtp.starttls.enable", mailStartTLS);
        props.put("mail.smtp.host", mailSMTPHost);
        props.put("mail.smtp.port", mailSMTPPort);
        props.put("mail.debug", mailDebug);
        props.put("mail.transport.protocol", mailTransport);

        Session session = Session.getInstance(props,
          new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailAccountUsername, mailAccountPassword);
            }
          });

        String errorInEmail = null;
        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailFrom));
            if(toEmail != null) {
                errorInEmail = toEmail;
                message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(toEmail));
            }
            if(bccEmail != null) {
                errorInEmail = bccEmail;
                message.setRecipients(Message.RecipientType.BCC,
                InternetAddress.parse(bccEmail));
            }
            message.setSubject(emailSubject);
            message.setContent(messageOut, "text/html");
            Transport.send(message);

        } catch ( AuthenticationFailedException e) {
            String errorMsg = "Problem Sending eMail to {} : Issue:Authentication Failed at  mail Service";
            LOG.warn(errorMsg);
            throw new NotificationException(errorMsg);
        } catch (MessagingException e) {
            LOG.warn("Problem Sending eMail to {} : Issue: {}", errorInEmail, e.getMessage());
            throw new NotificationException(e.getMessage());
        }
    }
}
