package net.respectnetwork.sdk.csp.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import net.respectnetwork.sdk.csp.BasicCSP;
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.CSPInformation;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;

public class ExampleCheckCloudNames {

	/* CHOOSE THE INDIVIDUAL's CLOUD NUMBER HERE */
	private static CloudNumber cloudNumber;

	static {

		try {

			System.out.print("Enter Cloud Number: ");
			cloudNumber = CloudNumber.create(new BufferedReader(new InputStreamReader(System.in)).readLine());

			if (cloudNumber == null) {
				
				System.err.println("Invalid Cloud Number.");
				System.exit(0);
			}
		} catch (IOException ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static void main(String[] args) throws Exception {

		// Step 0: Set up CSP

		CSPInformation cspInformation = new CSPInformationRespectNetwork();
		//CSPInformation cspInformation = new CSPInformationTTCC();

		CSP csp = new BasicCSP(cspInformation);

		// step 1: Check what Cloud Names exist for the Cloud Number

		CloudName[] cloudNames = csp.checkCloudNamesInCSP(cloudNumber);

		// done

		System.out.println("For Cloud Number " + cloudNumber + " found Cloud Names: " + Arrays.asList(cloudNames));
	}
}
