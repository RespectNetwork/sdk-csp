package net.respectnetwork.sdk.csp.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

import net.respectnetwork.sdk.csp.BasicCSP;
import net.respectnetwork.sdk.csp.BasicCSPInformation;
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.discount.NeustarRNCampaignCode;
import net.respectnetwork.sdk.csp.discount.NeustarRNDiscountCode;
import xdi2.core.syntax.CloudName;
import xdi2.core.syntax.CloudNumber;

public class ExampleRegisterAdditionalNames {

	/* Existing Clould Number that we are adding a Name To */
	private static CloudNumber existingCloudNumber;

	/* New Cloud Name to Register */
	private static CloudName additionalCloudName;

	/* CHOOSE THE INDIVIDUAL's SECRET TOKEN HERE */
	private static String existingUserSecretToken = "mysecret";


	/* Discount Code */
	private static NeustarRNDiscountCode additionalNameDiscountCode = NeustarRNDiscountCode.FirstFiveNames;
	
	/* Campaign Code */
	private static NeustarRNCampaignCode additionalNameRNCampaignCode = NeustarRNCampaignCode.FirstMillion;


	static {

		try {

			System.out.print("Enter Existing CloudNumber ( To add name to ): ");
			
			String cloudNumberString = new BufferedReader(new InputStreamReader(System.in)).readLine();
			existingCloudNumber = CloudNumber.create(cloudNumberString);
			
			//@TODO Check that  the ClouldNumber is already  Registered???
			if (existingCloudNumber == null) {

				System.err.println("Invalid Cloud Number.");
				System.exit(0);
			}
			
		    System.out.print("Enter Additional CloudName: ");

			String cloudNameString = new BufferedReader(new InputStreamReader(System.in)).readLine();
			if (cloudNameString.trim().isEmpty()) cloudNameString = "=dev.test." + UUID.randomUUID().toString();
			additionalCloudName = CloudName.create(cloudNameString);

			//@TODO Check that theCloudName is available.??

			if (additionalCloudName == null) {

				System.err.println("Invalid Cloud Name.");
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

		// Step 1: Register Additional Cloud Name and Create Member Graph Entry.
		
		
		// Question: Is this sufficient? There are Non RN CloudNumbers
		CloudNumber registeredCloudNumber = csp.checkCloudNameInRN(additionalCloudName);
		
		if (registeredCloudNumber != null ) {
			System.out.println(additionalCloudName + " is already  registered.");
			System.exit(0);
		}

		csp.registerAdditionalCloudNameInRN( additionalCloudName, existingCloudNumber, additionalNameDiscountCode, additionalNameRNCampaignCode );
		
        // Step  2: Register Additional Cloud Name in User Graph
		
		csp.registerAdditionalCloudNameInCloud(additionalCloudName, existingCloudNumber, existingUserSecretToken);
		
		//Step 3 Register Additional Cloud Name in CSP  Graph.
		
		csp.registerAdditionalCloudNameInCSP(additionalCloudName, existingCloudNumber);

		
		
		

		System.out.println("Done registering Additional Cloud Name " + additionalCloudName + " with Cloud Number " + existingCloudNumber );
	}
}
