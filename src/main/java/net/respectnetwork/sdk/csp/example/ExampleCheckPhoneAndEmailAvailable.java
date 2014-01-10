package net.respectnetwork.sdk.csp.example;

import java.util.Arrays;

import net.respectnetwork.sdk.csp.BasicCSP;
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.CSPInformation;
import net.respectnetwork.sdk.csp.TrustAllTrustManager;
import xdi2.core.xri3.CloudNumber;

public class ExampleCheckPhoneAndEmailAvailable {

	/* CHOOSE THE INDIVIDUAL's VERIFIED PHONE NUMBER HERE */
	private static String verifiedPhone = "123456";

	/* CHOOSE THE INDIVIDUAL's VERIFIED EMAIL HERE */
	private static String verifiedEmail = "test@test.com";

	public static void main(String[] args) throws Exception {

		TrustAllTrustManager.trustAll();

		// Step 0: Set up CSP

		CSPInformation cspInformation = new CSPInformationRespectNetwork();
		//CSPInformation cspInformation = new CSPInformationTTCC();

		CSP csp = new BasicCSP(cspInformation);

		// Step 1: Check if the phone number and e-mail address are available

		CloudNumber[] existingCloudNumbers = csp.checkPhoneAndEmailAvailableInRN(verifiedPhone, verifiedEmail);

		// done

		System.out.println("For verified phone number " + verifiedPhone + " and verified e-mail address " + verifiedEmail + " found Cloud Numbers: " + Arrays.asList(existingCloudNumbers));
	}
}
