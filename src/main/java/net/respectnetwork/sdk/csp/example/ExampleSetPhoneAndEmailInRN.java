package net.respectnetwork.sdk.csp.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.respectnetwork.sdk.csp.BasicCSP;
import net.respectnetwork.sdk.csp.BasicCSPInformation;
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.CSPInformation;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;

public class ExampleSetPhoneAndEmailInRN {

	/* CHOOSE THE INDIVIDUAL's PHONE NUMBER HERE */
	private static String phone = "RNPhone-" + UUID.randomUUID().toString();

	/* CHOOSE THE INDIVIDUAL's EMAIL HERE */
	private static String email = "RNEmail" + UUID.randomUUID().toString() + "@test.com";

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
			
			// Step 3: Set phone & email
			csp.setPhoneAndEmailInRN(cloudNumber, phone, email);
			System.out.println("Set phone & email for cloudNumber: "+cloudNumber.getXri().toString());
			
			// Step 3: Verify phone & email is set for correct cloudNumber
			CloudNumber[] existingCloudNumbers = csp.checkPhoneAndEmailAvailableInRN(phone, email);
			Set<CloudNumber> existingCloudNumbersSet  = new HashSet<CloudNumber>();
			existingCloudNumbersSet.addAll(Arrays.asList(existingCloudNumbers));
			
	
			System.out.println("Cloud Numbers for verifiedPhone & verifiedEmail  is "+existingCloudNumbersSet.size());
			if(existingCloudNumbersSet.size()==1 && existingCloudNumbersSet.iterator().next().equals(cloudNumber)){
				System.out.println("Verified phone & email  for cloudNUmber: "+cloudNumber.getXri().toString());
			}
		}else{
			System.out.println("Cloud Name : "+cloudName+" does not exist");
		}
	}
}
