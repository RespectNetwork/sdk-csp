package net.respectnetwork.sdk.csp.performance;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.respectnetwork.sdk.csp.BasicCSP;
import net.respectnetwork.sdk.csp.BasicCSPInformation;
import net.respectnetwork.sdk.csp.CSP;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.util.XDIClientUtil;
import xdi2.core.syntax.CloudName;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;

public class CreateDependent extends AbstractTester {
    
    /* CHOOSE THE GUARDIAN'S CLOUD NAME HERE */
    private  CloudName guardianCloudName;
    
    /* CHOOSE THE DEPENDENT'S CLOUD NAME HERE */
    private  CloudName dependentCloudName;
    
    /* CHOOSE THE DEPENDENT'S DOB */
    private  Date dependentBirthDate;
    
    /* CHOOSE THE INDIVIDUAL's SECRET TOKEN HERE */
    private String guardianToken;
    
    /* CHOOSE THE INDIVIDUAL's SECRET TOKEN HERE */
    private String dependentToken;


    /**
     * Constructor with BasicCSPInformation Arg.
     * @param cspInformation
     */
    public CreateDependent() {
    }
    
    /**
     * @return the guardianCloudName
     */
    public CloudName getGuardianCloudName() {
        return this.guardianCloudName;
    }

    /**
     * @param guardianCloudName the guardianCloudName to set
     */
    public void setGuardianCloudName(CloudName guardianCloudName) {
        this.guardianCloudName = guardianCloudName;
    }

    /**
     * @return the dependentCloudName
     */
    public CloudName getDependentCloudName() {
        return this.dependentCloudName;
    }

    /**
     * @param dependentCloudName the dependentCloudName to set
     */
    public void setDependentCloudName(CloudName dependentCloudName) {
        this.dependentCloudName = dependentCloudName;
    }

    /**
     * @return the dependentBirthDate
     */
    public Date getDependentBirthDate() {
        return dependentBirthDate;
    }

    /**
     * @param dependentBirthDate the dependentBirthDate to set
     */
    public void setDependentBirthDate(Date dependentBirthDate) {
        this.dependentBirthDate = dependentBirthDate;
    }



    /**
     * @return the guardianToken
     */
    public String getGuardianToken() {
        return guardianToken;
    }


    /**
     * @param guardianToken the guardianToken to set
     */
    public void setGuardianToken(String guardianToken) {
        this.guardianToken = guardianToken;
    }


    /**
     * @return the dependentToken
     */
    public String getDependentToken() {
        return dependentToken;
    }


    /**
     * @param dependentToken the dependentToken to set
     */
    public void setDependentToken(String dependentToken) {
        this.dependentToken = dependentToken;
    }


    public void execute() throws TestException {               

        // Step 0: Set up CSP
        
        CloudNumber guardianCloudNumber = null;
        CloudNumber dependentCloudNumber = null;
        PrivateKey guardianPrivateKey = null;
        PrivateKey dependentPrivateKey = null;
        
        boolean withConsent = true;


        BasicCSPInformation cspInformation = super.getCspInformation();
        
        CSP csp = new BasicCSP(cspInformation);

        
        XDIDiscoveryClient discovery = cspInformation.getXdiDiscoveryClient();

            
            try {
                XDIDiscoveryResult guardianRegistry = discovery.discoverFromRegistry(
                        XDIAddress.create(guardianCloudName.toString()));
                
                XDIDiscoveryResult dependentRegistry = discovery.discoverFromRegistry(
                        XDIAddress.create(dependentCloudName.toString()));
                
                guardianCloudNumber = guardianRegistry.getCloudNumber();
                dependentCloudNumber = dependentRegistry.getCloudNumber();
                
                guardianPrivateKey = XDIClientUtil.retrieveSignaturePrivateKey(guardianCloudNumber, guardianRegistry.getXdiEndpointUri(), guardianToken);
                System.out.println("GuardianPrivateKey Algo: " + guardianPrivateKey.getAlgorithm());

                dependentPrivateKey = XDIClientUtil.retrieveSignaturePrivateKey(dependentCloudNumber, dependentRegistry.getXdiEndpointUri(), dependentToken);
                System.out.println("DependentPrivateKey Algo: " + dependentPrivateKey.getAlgorithm());
                

                // Set User Cloud Data
                //csp.setGuardianshipInCloud(cspInformation, guardianCloudNumber, dependentCloudNumber, dependentBirthDate, withConsent, guardianToken, guardianPrivateKey);
                
                // Set CSP Cloud Data
                //csp.setGuardianshipInCSP(cspInformation, guardianCloudNumber, dependentCloudNumber, dependentBirthDate, withConsent, guardianPrivateKey);
                
                // Set MemberGraph Data
                csp.setGuardianshipInRN(cspInformation, guardianCloudNumber, dependentCloudNumber, dependentBirthDate, withConsent, guardianPrivateKey);
                
                System.out.println("Created Dependency : for Guardian (CloudNumber) " + guardianCloudNumber.toString());
        
        } catch ( Xdi2ClientException e ) {
            throw new TestException(e.getMessage());
        } catch ( GeneralSecurityException e ) {
            throw new TestException(e.getMessage());
        }



	}
    
    
    public void init() throws TestException {
        
        try {
            this.setGuardianCloudName(CloudName.create("=perf.test.db5yzssu4m"));
            this.setDependentCloudName(CloudName
                    .create("=perf.test.zxvejdamfk"));

            SimpleDateFormat format = new SimpleDateFormat("MM:dd:yyyy");
            Date dependentBirthDate = format.parse("10:10:1970");
            this.setDependentBirthDate(dependentBirthDate);

            this.setGuardianToken("mysecret");
            this.setDependentToken("mysecret");
            
        } catch (ParseException e) {
            System.out.println("Problem Parsing Date: " + e.getMessage());
        }
    }
   
}
