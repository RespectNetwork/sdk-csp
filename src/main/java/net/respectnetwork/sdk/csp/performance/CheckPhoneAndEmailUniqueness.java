package net.respectnetwork.sdk.csp.performance;

import java.util.Arrays;

import net.respectnetwork.sdk.csp.BasicCSP;
import net.respectnetwork.sdk.csp.BasicCSPInformation;
import net.respectnetwork.sdk.csp.CSP;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.xri3.CloudNumber;

public class CheckPhoneAndEmailUniqueness extends AbstractTester {
    
    /** Verified Email */
    private String verifiedEmail;
    
    /** Verified Phone */
    private String verifiedPhone;

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

    public CheckPhoneAndEmailUniqueness() {
        super();
    }

    public void execute() throws TestException {
        
        // Step 0: Set up CSP

        BasicCSPInformation cspInformation = super.getCspInformation();

        
        CSP csp = new BasicCSP(cspInformation);

        // Step 1: Check if the phone number and e-mail address are available
        try {
            if( verifiedEmail == null || verifiedPhone == null) {
                throw new TestException("Tester Data not set up: verifiedEmail: verifiedPhone ");
            }
            CloudNumber[] existingCloudNumbers = csp.checkPhoneAndEmailAvailableInRN(verifiedPhone, verifiedEmail);
            System.out.println("For verified phone number " + verifiedPhone + " and verified e-mail address " + verifiedEmail + " found Cloud Numbers: " + Arrays.asList(existingCloudNumbers));

        } catch ( Xdi2ClientException e ) {
            throw new TestException(e.getMessage());
        }

	}
    
    
    public void init() throws TestException {
        
        this.setVerifiedEmail("unm9rktrkb@perftest.com");
        this.setVerifiedPhone("+98638006275");
    
    }
    
    
}
