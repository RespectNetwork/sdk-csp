package net.respectnetwork.sdk.csp.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.respectnetwork.sdk.csp.BasicCSP;
import net.respectnetwork.sdk.csp.BasicCSPInformation;
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.discount.NeustarRNDiscountCode;
import net.respectnetwork.sdk.csp.discount.RespectNetworkRNDiscountCode;
import xdi2.core.constants.XDIConstants;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;
import xdi2.core.xri3.XDI3Segment;

public class ExampleRegisterBusinessName {

	/* BUSINESS CLOUD NUMBER  */
	private static CloudNumber businessCloudNumber;

	/* BUSINESS CLOUD NAME  */
	private static CloudName businessCloudName;
	
	/* BUSINESS Cloud Contact Cloud Number  */
	private static CloudNumber contactCloudNumber;


	static {

		try {

			System.out.print("Enter Business Cloud Name (leave blank for a random one): ");
			
			String cloudNameString = new BufferedReader(new InputStreamReader(System.in)).readLine();
			if (cloudNameString.trim().isEmpty()) cloudNameString = "+dev.test." + UUID.randomUUID().toString();
			businessCloudName = CloudName.create(cloudNameString);

			if (businessCloudName == null) {

				System.err.println("Invalid Cloud Name.");
				System.exit(0);
			}
			
			System.out.print("Enter Business Cloud Number (leave blank for a random one): ");
			String cloudNumberString = new BufferedReader(new InputStreamReader(System.in)).readLine();
			if (cloudNumberString.trim().isEmpty()) businessCloudNumber = CloudNumber.createRandom(businessCloudName.getCs());
           

			if (businessCloudNumber == null) {

				System.err.println("Invalid Business Cloud Number.");
				System.exit(0);
			}
			
			System.out.print("Enter Business Contact  Cloud Number (leave blank for a random one): ");
			String contactCloudNumberString = new BufferedReader(new InputStreamReader(System.in)).readLine();
			if (contactCloudNumberString.trim().isEmpty()) contactCloudNumber = CloudNumber.createRandom(XDIConstants.CS_AUTHORITY_PERSONAL);
           

			if (contactCloudNumber == null) {

				System.err.println("Invalid Contact Cloud Number.");
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

		// Step 1: Register Business Cloud Name and Create Member Graph Entry.
		
		// @TODO Question: Is this sufficient? There are Non RN CloudNumbers!!
		// Call Check Availability  Instead for  more Valid Test!!!
		CloudNumber registeredCloudNumber = csp.checkCloudNameAvailableInRN(businessCloudName);
		
		if (registeredCloudNumber != null ) {
			System.out.println(businessCloudName + " is already  registered.");
			System.exit(0);
		}
		
		csp.registerBusinessNameInRN(businessCloudName, businessCloudNumber, contactCloudNumber);
		
        // Step  2: Register Additional Cloud Name in CSP Graph

		csp.registerBusinessNameInCSP(businessCloudName, businessCloudNumber, contactCloudNumber);
		
        // Step  3: Register Additional Cloud Name in User Graph

		csp.registerBusinessNameInCloud(businessCloudName, businessCloudNumber, contactCloudNumber);

   		

		System.out.println("Done registering Business Cloud Name " + businessCloudName + " with Cloud Number " + businessCloudNumber );
	}
}
