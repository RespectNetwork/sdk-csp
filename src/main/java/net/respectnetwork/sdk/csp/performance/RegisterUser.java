package net.respectnetwork.sdk.csp.performance;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.respectnetwork.sdk.csp.BasicCSP;
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.discount.NeustarRNDiscountCode;
import net.respectnetwork.sdk.csp.discount.RespectNetworkRNDiscountCode;

import org.apache.commons.lang.RandomStringUtils;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.syntax.CloudName;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;

public class RegisterUser extends AbstractTester {
    
    
    /* CHOOSE THE INDIVIDUAL's CLOUD NUMBER HERE */
    private  CloudNumber cloudNumber;

    /* CHOOSE THE INDIVIDUAL's CLOUD NAME HERE */
    private  CloudName cloudName;

    /* CHOOSE THE INDIVIDUAL's SECRET TOKEN HERE */
    private  String secretToken = "mysecret";

    /* CHOOSE THE INDIVIDUAL's VERIFIED PHONE NUMBER HERE */
    private  String verifiedPhone; 

    /* CHOOSE THE INDIVIDUAL's VERIFIED EMAIL HERE */
    private  String verifiedEmail; 

	/* CHOOSE A NEUSTAR DISCOUNT CODE */
	private static NeustarRNDiscountCode neustarRNDiscountCode = NeustarRNDiscountCode.OnePersonOneName;

	/* CHOOSE A RESPECT NETWORK DISCOUNT CODE */
	private static RespectNetworkRNDiscountCode respectNetworkMembershipDiscountCode = RespectNetworkRNDiscountCode.IIW17;

    

    public RegisterUser() {
        super();
    }

    public void execute() throws TestException {
        
        // Step 0: Set up CSP

        CSP csp = new BasicCSP(super.cspInformation);
        
        // Step 1: Check if the phone number and e-mail address are available
        try {
            
            if( cloudNumber == null || cloudName == null || verifiedPhone == null || verifiedEmail == null) {
                throw new TestException("Tester Data not set up: cloudName : CloudNumber : verifiedEmail: verifiedPhone ");
            }
            System.out.println("Creating: cloudName: " + cloudName + " cloudNumber: " + cloudNumber + " verifiedEmail: " +  verifiedEmail + " verifiedPhone: " + verifiedPhone);

    		// Step 1: Register Cloud with Cloud Number and Shared Secret

    		csp.registerCloudInCSP(cloudNumber, secretToken);

    		// step 2: Set Cloud Services in Cloud

    		Map<XDIAddress, String> services = new HashMap<XDIAddress, String> ();

    		services.put(XDIAddress.create("<$https><$connect><$xdi>"), "https://mycloud-ote.neustar.biz/users/" + URLEncoder.encode(cloudNumber.toString(), "UTF-8") + "/connect/request");

    		csp.setCloudServicesInCloud(cloudNumber, secretToken, services);

    		// step 3: Check if the Cloud Name is available

    		boolean available = csp.checkCloudNameAvailableInRN(cloudName);

    		if (! available) throw new RuntimeException("Cloud Name " + cloudName + " is not available.");

    		// Step 4: Check if the phone number and e-mail address are available

    		CloudNumber[] existingCloudNumbers = csp.checkPhoneAndEmailAvailableInRN(verifiedPhone, verifiedEmail);

    		if (existingCloudNumbers[0] != null) throw new RuntimeException("This verified phone number is already registered with Cloud Number " + existingCloudNumbers[0] + ".");
    		if (existingCloudNumbers[1] != null) throw new RuntimeException("This verified e-mail address is already registered with Cloud Number " + existingCloudNumbers[1] + ".");

    		// step 5: Register Cloud Name

    		csp.registerCloudNameInRN(cloudName, cloudNumber, verifiedPhone, verifiedEmail, neustarRNDiscountCode);
    		csp.registerCloudNameInCSP(cloudName, cloudNumber);
    		csp.registerCloudNameInCloud(cloudName, cloudNumber, secretToken);


    		// step 6: Set phone number and e-mail address

    		csp.setPhoneAndEmailInCloud(cloudNumber, secretToken, verifiedPhone, verifiedEmail);

    		// step 7: Set RN/RF membership

    		csp.setRespectNetworkMembershipInRN(cloudNumber, new Date(), respectNetworkMembershipDiscountCode);
    		csp.setRespectFirstMembershipInRN(cloudNumber);

            // done

            System.out.println("Done registering Cloud Name " + cloudName + " with Cloud Number " + cloudNumber + " and " + services.size() + " services.");
        } catch ( Xdi2ClientException e ) {
            throw new TestException(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new TestException(e.getMessage());
        }

	}

    /**
     * @return the cloudNumber
     */
    public CloudNumber getCloudNumber() {
        return cloudNumber;
    }

    /**
     * @param cloudNumber the cloudNumber to set
     */
    public void setCloudNumber(CloudNumber cloudNumber) {
        this.cloudNumber = cloudNumber;
    }

    /**
     * @return the cloudName
     */
    public CloudName getCloudName() {
        return cloudName;
    }

    /**
     * @param cloudName the cloudName to set
     */
    public void setCloudName(CloudName cloudName) {
        this.cloudName = cloudName;
    }

    /**
     * @return the verifiedPhone
     */
    public String getVerifiedPhone() {
        return verifiedPhone;
    }

    /**
     * @param verifiedPhone the verifiedPhone to set
     */
    public void setVerifiedPhone(String verifiedPhone) {
        this.verifiedPhone = verifiedPhone;
    }

    /**
     * @return the verifiedEmail
     */
    public String getVerifiedEmail() {
        return verifiedEmail;
    }

    /**
     * @param verifiedEmail the verifiedEmail to set
     */
    public void setVerifiedEmail(String verifiedEmail) {
        this.verifiedEmail = verifiedEmail;
    }
    
    public void init() throws TestException {
        
        String randAN = RandomStringUtils.randomAlphanumeric(10).toLowerCase();
        String randN = RandomStringUtils.randomNumeric(11);
        
        this.verifiedEmail =  randAN + "@perftest.com";
        this.verifiedPhone = "+" + randN;
        
        String cloudNameString = "=perf.test." + UUID.randomUUID().toString();
        //cloudNameString = "=perf.test.1";
        CloudName cloudName = CloudName.create(cloudNameString);
        this.setCloudName(cloudName);
        CloudNumber cloudNumber = CloudNumber.createRandom(cloudName.getCs());
        this.setCloudNumber(cloudNumber);
    
    }


}
