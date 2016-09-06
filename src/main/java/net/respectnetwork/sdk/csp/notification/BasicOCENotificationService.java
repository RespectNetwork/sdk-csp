package net.respectnetwork.sdk.csp.notification;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Basic Notification Service uses
 *
 *    1) Twillio for SMS Notifications
 *    2) Java Mail Transport for EMail
 *
 */
public class BasicOCENotificationService extends BasicNotificationService {
    
    /** Class Logger */
    private static final Logger LOG = LoggerFactory
            .getLogger(BasicOCENotificationService.class);
    
    public static class KVPair {
    	public String emailAddress;
    	
   	
    }
    
    public static class PojoOCE {
    	public ArrayList<KVPair> getTo() {
			return to;
		}
		public void setTo(ArrayList<KVPair> to) {
			this.to = to;
		}
		public KVPair getFrom() {
			return from;
		}
		public void setFrom(KVPair from) {
			this.from = from;
		}
		public String getMimeType() {
			return mimeType;
		}
		public void setMimeType(String mimeType) {
			this.mimeType = mimeType;
		}
		public String getSubject() {
			return subject;
		}
		public void setSubject(String subject) {
			this.subject = subject;
		}
		public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}
		public ArrayList<KVPair> to;
    	public KVPair from;
    	public String mimeType;
    	public String subject;
    	public String content;
    }
    
    private String OCEApiKey;
    private String OCESecret;
    private String OCEendpoint;    
    
    public String getOCEendpoint() {
		return OCEendpoint;
	}

	public void setOCEendpoint(String oCEendpoint) {
		OCEendpoint = oCEendpoint;
	}
    
    public String getOCEApiKey() {
		return OCEApiKey;
	}

	public void setOCEApiKey(String oCEApiKey) {
		OCEApiKey = oCEApiKey;
	}

	public String getOCESecret() {
		return OCESecret;
	}

	public void setOCESecret(String oCESecret) {
		OCESecret = oCESecret;
	}

    private PojoOCE _pojo;
 
    @Override
    public void sendEmailNotification(String emailTo, String messageOut, String subject)
            throws NotificationException {
        // NOT mt-safe
        _pojo = new PojoOCE();

    	LOG.debug("sendOCEMail[" + OCEendpoint + "] with subject:" + getEmailSubject()  + " emailFrom:" + getEmailFrom() + " and sendTo:" + emailTo);
            // locally we need an HTTP/POST/JSON/ACCEPT/AUTHORIZATION instances.
    		// which if any of these are thread-safe for reuse?
        try {
        	// TODO: remove this once working from spring ... 
        	String endpoint = this.getOCEendpoint() ; //"https://internalapi.nexgen.neustar.biz/oce/v1/email";
        	HttpClient client = new DefaultHttpClient();
        	HttpPost   post = new HttpPost(endpoint);

        	//messageOut = updateHtmlMarkup(messageOut);
        	String jsonPost = generatePostJson(emailTo, messageOut);
        	LOG.debug("OCE JSON string to send as request:" + jsonPost);
          	StringEntity jsonPostEntity = new StringEntity(jsonPost);
        	post.setHeader(HTTP.CONTENT_TYPE, "application/json");
        	
        	post.setEntity(jsonPostEntity);
        	String authHdr = generateAuthorizationHeader(this.OCEApiKey,this.OCESecret);
        	post.addHeader("Authorization", authHdr);
        	HttpResponse rsp = client.execute(post);
        	LOG.info(String.valueOf(rsp.getStatusLine().getStatusCode()));
        	LOG.debug(rsp.getStatusLine().getReasonPhrase());
        	
        } // FIXME: types of exceptions caught. 
        catch ( NotificationException e) { 
            String errorMsg = "Problem Sending eMail to {} : Issue:Authentication Failed at  mail Service";
            LOG.warn(errorMsg);
            throw new NotificationException(errorMsg);
        } catch (Exception e) {
            LOG.warn("Problem Sending eMail to {} : Issue: {}", emailTo, e.getMessage());
            throw new NotificationException(e.getMessage());
        }
    }
      
    private String updateHtmlMarkup (String message){
    	// no \n only <br>  <bold> the code following the git-code place holder
    	String newMsg = message.replaceAll("\\n", "<br/>");
    	
    	return newMsg;
    }
    
	private String generatePostJson(String emailTo, String messageOut) throws JsonMappingException, IOException {
		String pic = "<img width=\"275\" height=\"96\" src=\"data:image/png;base64,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\" />";
		KVPair p = new KVPair();
		p.emailAddress = emailTo;
		ArrayList<KVPair> a = new ArrayList<KVPair> ();
		a.add(p);
		_pojo.setTo(a);
		KVPair z = new KVPair();
		z.emailAddress = getEmailFrom();

		_pojo.setFrom(z);
		_pojo.setMimeType("HTML");
		_pojo.setSubject(getEmailSubject());
		_pojo.setContent("<html>" + messageOut + /*pic +*/ "</html>");
		ObjectMapper om = new ObjectMapper();	
		//Gson gson = new Gson();
		return om.writeValueAsString(_pojo);
	}

   
    /*
     * NexGen [Neustar Inc.] 2014 -
     * 
     * mailto://will.martin@neustar.biz
     * 
     */
	public String generateAuthorizationHeader(String apiKey, String sharedSecret) throws NotificationException {
	    String REALM="Neustar";
	    if (apiKey == null) {
	        throw new IllegalArgumentException("API Key is null");
	    }
	    if (sharedSecret == null) {
	        throw new IllegalArgumentException("Shared secret is null");
	    }
	    // Get the current epoch time in seconds
	    long epoch = System.currentTimeMillis() / 1000;
	    String timestamp = String.valueOf(epoch);
	    String data = timestamp + apiKey + sharedSecret;
	    // Generate the Hmac value
	    byte[] hmacData = null;
	    String base64encoded = null;
	    try {
	        //javax.crypto.spec.SecretKeySpec
	        SecretKeySpec secretKey = new SecretKeySpec(sharedSecret.getBytes("UTF-8"), "HmacSHA256");
	        //javax.crypto.Mac
	        Mac mac = Mac.getInstance("HmacSHA256");
	        mac.init(secretKey);
	        mac.update(data.getBytes("UTF-8"));
	        hmacData = mac.doFinal();
	        // Base64-encode the result; org.apache.commons.codec.binary.Base64
	        // Internal Neustar document baselines Base64 at code 1.7, we have 1.4 in our
	        // Maven depdencies. Will this be an issue?
	        base64encoded = new String(Base64.encodeBase64(hmacData), "UTF-8");
	    } catch (NoSuchAlgorithmException e) {
	        throw new NotificationException("Crypto Algorithm not found: " +e.getLocalizedMessage());
	    } catch (UnsupportedEncodingException e) {
	    	throw new NotificationException("Encoding unexpected. Wanted UTF-8: " +e.getLocalizedMessage());
	    } catch (InvalidKeyException e) {
	    	throw new NotificationException("Invalid Key exception: " + e.getLocalizedMessage());
	    }
	 
	    // Create the full header value
	    return String.format("%s %s:%s:%s", REALM, timestamp, apiKey, base64encoded);
	}

	
	/*
	 * deadman funtional test main. DO NOT script.
	 */
	
	public static void main(String[] args){
		BasicOCENotificationService bon = new BasicOCENotificationService();
		
		bon.setEmailFrom("will.martin@neustar.biz");
		bon.setEmailSubject("Test OCE send within csp-sdk codebase");
		
		//bon.setOCEApiKey("p9Es6G3w_ib5bC9BRDdmyd5lxkIa");
		//bon.setOCESecret("pyIpbBQ08GGtzP61GMrEE_4D900a");
		bon.setOCEApiKey("d7XiM_gChK_VHHh4lybMPKsHusoa"/*"p9Es6G3w_ib5bC9BRDdmyd5lxkIa"*/);
		bon.setOCESecret("7OmxVvWTlC2z_l0FzwIIkY7WX90a"/*"pyIpbBQ08GGtzP61GMrEE_4D900a"*/);
		
		try {
			bon.sendEmailNotification("will.martin@neustar.biz","A bunch of gobbledy gook", "");
		} catch (NotificationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
}
