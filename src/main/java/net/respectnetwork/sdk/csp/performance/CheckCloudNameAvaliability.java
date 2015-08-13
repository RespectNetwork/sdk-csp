package net.respectnetwork.sdk.csp.performance;

import net.respectnetwork.sdk.csp.BasicCSP;
import net.respectnetwork.sdk.csp.BasicCSPInformation;
import net.respectnetwork.sdk.csp.CSP;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.syntax.CloudName;

public class CheckCloudNameAvaliability extends AbstractTester {

    /** CloudName */
    private String cloudName;

    /**
     * @return the CloudName
     */
    public String getCloudName() {
        return cloudName;
    }

    /**
     * @param verifiedEmail
     *            the verifiedEmail to set
     */
    public void setCloudName(String cloudName) {
        this.cloudName = cloudName;
    }

    public CheckCloudNameAvaliability() {
    }

    public void execute() throws TestException {

        boolean available;

        try {

            BasicCSPInformation cspInformation = super.getCspInformation();

            CSP csp = new BasicCSP(cspInformation);
            available = csp.checkCloudNameAvailableInRN(CloudName.create(this.cloudName));

        } catch (Xdi2ClientException e) {
            throw new TestException(e.getMessage());
        }

        if (!available) {
            System.out.println("Cloud Name " + this.cloudName + " is not available.");
        } else {
            System.out.println("Cloud Name " + this.cloudName + " is available.");
        }

    }

    public void init() throws TestException {
        this.setCloudName("=beech");
    }

}
