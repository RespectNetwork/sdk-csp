package net.respectnetwork.sdk.csp.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import net.respectnetwork.sdk.csp.BasicCSP;
import net.respectnetwork.sdk.csp.BasicCSPInformation;
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.CSPInformation;
import xdi2.core.syntax.CloudName;
import xdi2.core.syntax.CloudNumber;

public class ExampleMemberRegistrar {
	
	/* CHOOSE rn csp secret token here */
	private static String rnCspSecretToken = "testcsp";
	
	/* CHOOSE THE INDIVIDUAL's CLOUD NUMBER HERE */
	private static CloudNumber cloudNumber;

	/* CHOOSE THE INDIVIDUAL's CLOUD NAME HERE */
	private static CloudName cloudName;

	
	static {

		try {

			System.out.print("Enter Cloud Name: ");
			cloudName = CloudName.create(new BufferedReader(new InputStreamReader(System.in)).readLine());

			if (cloudName == null) {
				
				System.err.println("Invalid Cloud Name.");
				System.exit(0);
			}
		} catch (IOException ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public static void main(String[] args) throws Exception {

		// Step 0: Set up CSP
		CSPInformation cspInformation = new CSPInformationTestCSPOTE();

		CSP csp = new BasicCSP(cspInformation);

		// Step 1: Check cloudName is in RN
		cloudNumber = csp.checkCloudNameInRN(cloudName);		

		if(cloudNumber!=null){			
			// Step 2: set cspsecret token to null & set RnCspSecretToken as it is required to set phone and email
			((BasicCSPInformation) csp.getCspInformation()).setCspSignaturePrivateKey(null);
			((BasicCSPInformation) csp.getCspInformation()).setRnCspSecretToken(rnCspSecretToken);
			//Step 3:get member registrar
			CloudNumber cspCloudNumber = csp.getMemberRegistrar(cloudNumber);
			System.out.println("Registrar for cloudNumber "+ cloudNumber +": "+cspCloudNumber);
		}else{
			System.out.println("Cloud Name : "+cloudName+" does not exist");
		}
	}
}
