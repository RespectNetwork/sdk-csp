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
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.CSP.NeustarRnDiscountCode;
import net.respectnetwork.sdk.csp.CSPInformation;
import net.respectnetwork.sdk.csp.TrustAllTrustManager;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;
import xdi2.core.xri3.XDI3Segment;

public class ExampleRegister {

	/* CHOOSE THE INDIVIDUAL's CLOUD NUMBER HERE */
	private static CloudNumber cloudNumber;

	/* CHOOSE THE INDIVIDUAL's CLOUD NAME HERE */
	private static CloudName cloudName;

	/* CHOOSE THE INDIVIDUAL's SECRET TOKEN HERE */
	private static String secretToken = "mysecret";

	/* CHOOSE THE INDIVIDUAL's VERIFIED PHONE NUMBER HERE */
	private static String verifiedPhone = "myphone-" + UUID.randomUUID().toString();

	/* CHOOSE THE INDIVIDUAL's VERIFIED EMAIL HERE */
	private static String verifiedEmail = "test" + UUID.randomUUID().toString() + "@test.com";

	static {

		try {

			System.out.print("Enter Cloud Name: ");
			cloudName = CloudName.create(new BufferedReader(new InputStreamReader(System.in)).readLine());

			if (cloudName == null) {
				
				System.err.println("Invalid Cloud Name.");
				System.exit(0);
			}

			cloudNumber = CloudNumber.createRandom(cloudName.getCs());
		} catch (IOException ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static void main(String[] args) throws Exception {

		TrustAllTrustManager.trustAll();

		// Step 0: Set up CSP

		CSPInformation cspInformation = new CSPInformationRespectNetwork();
		//CSPInformation cspInformation = new CSPInformationTTCC();

		CSP csp = new BasicCSP(cspInformation);

		// Step 1: Register Cloud with Cloud Number and Shared Secret

		csp.registerCloudInCSP(cloudNumber, secretToken);

		// step 2: Set Cloud Services in Cloud

		Map<XDI3Segment, String> services = new HashMap<XDI3Segment, String> ();

		services.put(XDI3Segment.create("<$https><$connect><$xdi>"), "http://mycloud-ote.neustar.biz:8085/personalclouds/" + URLEncoder.encode(cloudNumber.toString(), "UTF-8") + "/connect/request");

		csp.setCloudServicesInCloud(cloudNumber, secretToken, services);

		// step 3: Check if the Cloud Name is available

		CloudNumber existingCloudNumber = csp.checkCloudNameAvailableInRN(cloudName);

		if (existingCloudNumber != null) throw new RuntimeException("Cloud Name " + cloudName + " is already registered with Cloud Number " + existingCloudNumber + ".");

		// Step 4: Check if the phone number and e-mail address are available

		CloudNumber[] existingCloudNumbers = csp.checkPhoneAndEmailAvailableInRN(verifiedPhone, verifiedEmail);

		if (existingCloudNumbers[0] != null) throw new RuntimeException("This verified phone number is already registered with Cloud Number " + existingCloudNumbers[0] + ".");
		if (existingCloudNumbers[1] != null) throw new RuntimeException("This verified e-mail address is already registered with Cloud Number " + existingCloudNumbers[1] + ".");

		// step 5: Register Cloud Name

		csp.registerCloudNameInRN(cloudName, cloudNumber, verifiedPhone, verifiedEmail, NeustarRnDiscountCode.OnePersonOneName);
		csp.registerCloudNameInCSP(cloudName, cloudNumber);
		csp.registerCloudNameInCloud(cloudName, cloudNumber, secretToken);

		// step 6: Set phone number and e-mail address

		csp.setPhoneAndEmailInCloud(cloudNumber, secretToken, verifiedPhone, verifiedEmail);

		// step 7: Set RN/RF/RFL membership

		csp.setRespectNetworkMembershipInRN(cloudNumber);
		csp.setRespectFirstMembershipInRN(cloudNumber, new Date());
		csp.setRespectFirstLifetimeMembershipInRN(cloudNumber);

		// done

		System.out.println("Done registering Cloud Name " + cloudName + " with Cloud Number " + cloudNumber + " and " + services.size() + " services.");
	}
}
