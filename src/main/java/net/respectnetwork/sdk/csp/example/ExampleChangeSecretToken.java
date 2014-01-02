package net.respectnetwork.sdk.csp.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import net.respectnetwork.sdk.csp.BasicCSP;
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.CSPInformation;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;

public class ExampleChangeSecretToken {

	/* CHOOSE THE INDIVIDUAL's CLOUD NAME HERE */
	private static CloudName cloudName;

	/* CHOOSE THE INDIVIDUAL's SECRET TOKEN HERE */
	private static String secretToken = "mynewsecret";

	static {

		try {

			System.out.print("Enter Cloud Name: ");
			cloudName = CloudName.create(new BufferedReader(new InputStreamReader(System.in)).readLine());
		} catch (IOException ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

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
