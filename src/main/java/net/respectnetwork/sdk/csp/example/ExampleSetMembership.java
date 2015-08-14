package net.respectnetwork.sdk.csp.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import net.respectnetwork.sdk.csp.BasicCSP;
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.CSPInformation;
import net.respectnetwork.sdk.csp.discount.RespectNetworkRNDiscountCode;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.syntax.CloudName;
import xdi2.core.syntax.CloudNumber;

public class ExampleSetMembership {

	/* CHOOSE THE INDIVIDUAL's CLOUD NAME HERE */
	private static CloudName cloudName;

	/* CHOOSE A RESPECT NETWORK DISCOUNT CODE */
	private static RespectNetworkRNDiscountCode respectNetworkMembershipDiscountCode = RespectNetworkRNDiscountCode.IIW17;

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

	public static void main(String[] args) throws Xdi2ClientException {

		CloudNumber cloudNumber;

		// Step 0: Set up CSP

		CSPInformation cspInformation = new CSPInformationTestCSPOTE();

		CSP csp = new BasicCSP(cspInformation);

		// Step 1: Look for the Cloud Name's Cloud Number
		// If we already know the Cloud Number, then this step can be omitted.

		cloudNumber = csp.checkCloudNameInRN(cloudName);

		if (cloudNumber == null) throw new RuntimeException("Cloud Name " + cloudName + " does not exist.");

		// Step 2: Set Membership

		csp.setRespectNetworkMembershipInRN(cloudNumber, new Date(), respectNetworkMembershipDiscountCode);
		csp.setRespectFirstMembershipInRN(cloudNumber);

		// done

		System.out.println("Done setting RN membership for Cloud Name " + cloudName);
	}
}
