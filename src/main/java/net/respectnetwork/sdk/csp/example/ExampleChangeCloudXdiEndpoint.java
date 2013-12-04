package net.respectnetwork.sdk.csp.example;

import net.respectnetwork.sdk.csp.BasicCSP;
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.CSPInformation;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;

public class ExampleChangeCloudXdiEndpoint {

	/* CHOOSE THE INDIVIDUAL's CLOUD NAME HERE */
	private static CloudName cloudName = CloudName.create("=dev.test.66");

	/* CHOOSE THE INDIVIDUAL's XDI ENDPOINT HERE */
	private static String cloudXdiEndpoint = "http://xdi.mycsp.com/mynewendpoint";

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

		// Step 2: Change XDI endpoint

		csp.setCloudXdiEndpointInRN(cloudNumber, cloudXdiEndpoint);

		// done

		System.out.println("Done setting XDI endpoint for Cloud Name " + cloudName);
	}
}
