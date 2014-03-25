package net.respectnetwork.sdk.csp.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.PrivateKey;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.respectnetwork.sdk.csp.BasicCSP;
import net.respectnetwork.sdk.csp.BasicCSPInformation;
import net.respectnetwork.sdk.csp.CSP;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.util.XDIClientUtil;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;
import xdi2.core.xri3.XDI3Segment;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;

public class ExampleCreateGuardianship {


	/* CHOOSE THE GUARDIAN'S CLOUD NAME HERE */
	private static CloudName guardianCloudName;
	
	/* CHOOSE THE DEPENDENT'S CLOUD NAME HERE */
    private static CloudName dependentCloudName;
    
    /* CHOOSE THE DEPENDENT'S DOB */
    private static Date dependentBirthDate;
    
    /* CHOOSE THE INDIVIDUAL's SECRET TOKEN HERE */
    private static String guardianToken = "mysecret";
    
    /* CHOOSE THE INDIVIDUAL's SECRET TOKEN HERE */
    private static String dependentToken = "mysecret";


	static {

		try {

            System.out.print("Enter Guardian Cloud Name: ");
            guardianCloudName = CloudName.create(new BufferedReader(new InputStreamReader(System.in)).readLine());
            
            if (guardianCloudName == null) {
                System.err.println("Invalid Cloud Name.");
                System.exit(0);
            }
 		
	        System.out.print("Enter Dependent Cloud Name: ");
	        dependentCloudName = CloudName.create(new BufferedReader(new InputStreamReader(System.in)).readLine());
            
            if (dependentCloudName == null) {

                System.err.println("Invalid Cloud Name.");
                System.exit(0);
            }
	            
	        System.out.print("Enter Dependent Date Of Birth (MM:dd:yyyy): ");
	        try {
	            SimpleDateFormat format = 
	                    new SimpleDateFormat("MM:dd:yyyy");
	            dependentBirthDate = format.parse(new BufferedReader(new InputStreamReader(System.in)).readLine());
	        } catch (ParseException e) {
                System.err.println("Invalid Date.");
                System.exit(0);
	        }
	            
	            

		} catch (IOException ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static void main(String[] args) throws Exception {

		// Step 0: Set up CSP

		BasicCSPInformation cspInformation = new CSPInformationTestCSPOTE();

		cspInformation.retrieveCspSignaturePrivateKey();
		cspInformation.setRnCspSecretToken(null);

		CSP csp = new BasicCSP(cspInformation);
		
		// Common Data
		boolean withConsent = true;
		
		// Resolve Cloud Numbers from Name
		
        XDIDiscoveryClient discovery = cspInformation.getXdiDiscoveryClient();
        
        CloudNumber guardianCloudNumber = null;
        CloudNumber dependentCloudNumber = null;
        PrivateKey guardianPrivateKey = null;
        PrivateKey dependentPrivateKey = null;

        
        try {
            XDIDiscoveryResult guardianRegistry = discovery.discoverFromRegistry(
                    XDI3Segment.create(guardianCloudName.toString()), null);
            
            XDIDiscoveryResult dependentRegistry = discovery.discoverFromRegistry(
                    XDI3Segment.create(dependentCloudName.toString()), null);
            
            guardianCloudNumber = guardianRegistry.getCloudNumber();
            dependentCloudNumber = dependentRegistry.getCloudNumber();
            
            String guardianXdiEndpoint = guardianRegistry.getXdiEndpointUri();
            String dependentXdiEndpoint = dependentRegistry.getXdiEndpointUri();

            guardianPrivateKey = XDIClientUtil.retrieveSignaturePrivateKey(guardianCloudNumber, guardianXdiEndpoint, guardianToken);
            dependentPrivateKey = XDIClientUtil.retrieveSignaturePrivateKey(dependentCloudNumber, dependentXdiEndpoint, dependentToken);

            if (guardianCloudNumber == null || dependentCloudNumber == null) {
                System.err.println("Un-registered Cloud Name.");
                System.exit(0);
            }

        } catch (Xdi2ClientException e) {
            System.err.println("Problem with Cloud Name Provided.");
            System.exit(0);
        }
        
             
        try {
            // Set User Cloud Data
    	    csp.setGuardianshipInCloud(cspInformation, guardianCloudNumber, dependentCloudNumber, dependentBirthDate, withConsent, guardianToken, guardianPrivateKey);
    		
    		// Set CSP Cloud Data
    	    csp.setGuardianshipInCSP(cspInformation, guardianCloudNumber, dependentCloudNumber, dependentBirthDate, withConsent, guardianPrivateKey);
    	    
    	    // Set MemberGraph Data
    	    csp.setGuardianshipInRN(cspInformation, guardianCloudNumber, dependentCloudNumber, dependentBirthDate, withConsent, guardianPrivateKey);
    	     	    
    	    //Check the Results
    	    CloudNumber[] theDependents = csp.getMyDependentsInCSP(cspInformation, guardianCloudNumber);
    	    
    	    if (theDependents == null) {
    	        System.out.println("No Dependents found for " + guardianCloudName.toString()); 	        
    	    } else {	    
        	    for(int i=0; i < theDependents.length; i++){
        	        System.out.println("Dependent: " + i + " = " + theDependents[i]);
        	    }
    	    }
    	    
    	    
            CloudNumber[] theGuardians = csp.getMyGuardiansInCSP(cspInformation, dependentCloudNumber);
            
            if (theGuardians == null) {
                System.out.println("No Guardians found for " + dependentCloudName.toString() );         
            } else {        
                for(int i=0; i < theGuardians.length; i++){
                    System.out.println("Guardian: " + i + " = " + theGuardians[i]);
                }
            }
    	    
        } catch (Xdi2ClientException e) {
            System.out.println("Xdi2ClientException: " + e.getMessage());
            e.printStackTrace();
        }
	    
	   
	}
}
