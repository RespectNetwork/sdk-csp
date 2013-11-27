package net.respectnetwork.sdk.csp.example;

import net.respectnetwork.sdk.csp.BasicCSP;
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.CSPInformation;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.constants.XDIConstants;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;

public class ExampleRegister {

	/* CHOOSE THE INDIVIDUAL's CLOUD NUMBER HERE */
	private static CloudNumber cloudNumber = CloudNumber.createRandom(XDIConstants.CS_EQUALS);

	/* CHOOSE THE INDIVIDUAL's CLOUD NAME HERE */
	private static CloudName cloudName = CloudName.create("=dev.test.525");

	/* CHOOSE THE INDIVIDUAL's SECRET TOKEN HERE */
	private static String secretToken = "mysecret";

	public static void main(String[] args) throws Xdi2ClientException {

		// Step 0: Set up CSP

		CSPInformation cspInformation = new CSPInformationRespectNetwork();
		//CSPInformation cspInformation = new CSPInformationTTCC();

		CSP csp = new BasicCSP(cspInformation);

		// Step 1: Register Cloud with Cloud Number and Shared Secret

		csp.registerCloud(cloudNumber, secretToken);

		// step 2: Check if Cloud Name exists already

		CloudNumber existingCloudNumber = csp.checkCloudNameAvailable(cloudName);

		if (existingCloudNumber != null) throw new RuntimeException("Cloud Name " + cloudName + " is already registered with Cloud Number " + existingCloudNumber + ".");

		// step 3: Register Cloud Name

		csp.registerCloudName(cloudName, cloudNumber);

		// step 4: Register Cloud XDI endpoint with Cloud Number

		csp.setCloudXdiEndpoint(cloudNumber);

		// done

		System.out.println("Done registering Cloud Name " + cloudName + " with Cloud Number " + cloudNumber);
	}
}
