package net.respectnetwork.sdk.csp.example;

import net.respectnetwork.sdk.csp.BasicCSP;
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.CSPInformation;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;

public class ExampleChangeSecretToken {

	/* CHOOSE THE INDIVIDUAL's CLOUD NAME HERE */
	private static CloudName cloudName = CloudName.create("=dev.test.525");

	/* CHOOSE THE INDIVIDUAL's SECRET TOKEN HERE */
	private static String secretToken = "mynewsecret";

	public static void main(String[] args) throws Xdi2ClientException {

		CloudNumber cloudNumber;

		// Step 0: Set up CSP

		CSPInformation cspInformation = new CSPInformationRespectNetwork();
		//CSPInformation cspInformation = new CSPInformationTTCC();

		CSP csp = new BasicCSP(cspInformation);

		// Step 1: Look for the Cloud Name's Cloud Number
		// If we already know the Cloud Number, then this step can be omitted.

		cloudNumber = csp.checkCloudNameAvailableInRN(cloudName);

		if (cloudNumber == null) throw new RuntimeException("Cloud Name " + cloudName + " does not exist.");

		// Step 2: Change Secret Token

		csp.setCloudSecretTokenInCSP(cloudNumber, secretToken);

		// done

		System.out.println("Done setting secret token for Cloud Name " + cloudName);
	}
}
