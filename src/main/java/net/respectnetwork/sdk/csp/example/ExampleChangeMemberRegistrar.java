package net.respectnetwork.sdk.csp.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import net.respectnetwork.sdk.csp.BasicCSP;
import net.respectnetwork.sdk.csp.BasicCSPInformation;
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.CSPInformation;
import xdi2.core.syntax.CloudName;
import xdi2.core.syntax.CloudNumber;

public class ExampleChangeMemberRegistrar {
	
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
		CSPInformation cspInformation = new CSPInformationTestCSPStage();

		CSP csp = new BasicCSP(cspInformation);
		((BasicCSPInformation) csp.getCspInformation()).setCspSignaturePrivateKey(null);
		((BasicCSPInformation) csp.getCspInformation()).setRnCspSecretToken(rnCspSecretToken);
		
		// Step 1: Check cloudName is in RN
		cloudNumber = csp.checkCloudNameInRN(cloudName);

		if (cloudNumber != null) {
			CloudNumber cspCloudNumber = csp.getMemberRegistrar(cloudNumber);

			// Step 2: Set up new CSP
			CSPInformation cspInformation2 = new CSPInformationEmmettStage();
			CSP csp2 = new BasicCSP(cspInformation2);
			csp2.changeMemberRegistrarInRN(cloudNumber);

			// Step 3:get new member registrar
			cspCloudNumber = csp2.getMemberRegistrar(cloudNumber);
			//RND-193 change cloud end point in member graph use setCloudXdiEndpointInRN
			//csp2.setCloudXdiEndpointInRN(cloudNumber, cspInformation2.getCspCloudBaseXdiEndpoint()+URLEncoder.encode(cloudNumber.toString(), "UTF-8"));
			System.out.println("Registrar for cloudNumber " + cloudNumber + ": " + cspCloudNumber);
		} else {
			System.out.println("Cloud Name : " + cloudName + " does not exist");
		}

	}
}
