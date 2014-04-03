package net.respectnetwork.sdk.csp.performance;

import net.respectnetwork.sdk.csp.BasicCSPInformation;

public class AbstractTester implements Tester {
    
    /** CSP  Information */
    public BasicCSPInformation cspInformation;


    /**
     * Default  Constructor
     * 
     * @param cspInformation
     */
    public AbstractTester() {
        super();
    }


    /**
     * @return the cspInformation
     */
    public BasicCSPInformation getCspInformation() {
        return cspInformation;
    }


    /**
     * @param cspInformation the cspInformation to set
     */
    public void setCspInformation(BasicCSPInformation cspInformation) {
        this.cspInformation = cspInformation;
    }
    
    /**
     * 
     * @throws Exception
     */
    public void execute() throws TestException {

    }
    
    /**
     * 
     * @throws Exception
     */
    public void init() throws TestException {

    }
}
