package net.respectnetwork.sdk.csp.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import net.respectnetwork.sdk.csp.BasicCSP;
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.CSPInformation;
import xdi2.core.syntax.CloudName;
import xdi2.core.syntax.CloudNumber;

public class ExampleCloudTransferInCSP {

	/* CHOOSE THE INDIVIDUAL's CLOUD NUMBER HERE */
	private static CloudNumber cloudNumber;

	private static String secretToken = "mysecret";
	
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

		CSPInformation cspInformation = new CSPInformationTestCSPOTE();

		CSP csp = new BasicCSP(cspInformation);
		CloudName[] cloudNames = csp.checkCloudNamesInCSP(cloudNumber);
		

		CSPInformation cspInformation2 = new CSPInformationEmmettStage();
		CSP csp2 = new BasicCSP(cspInformation2);
		for(CloudName cloudName:cloudNames){
			System.out.println(cloudName.getXDIAddress().toString());
		}
		csp2.transferCloudInCSP(cloudNumber, cloudNames, secretToken);
		cloudNames = csp2.checkCloudNamesInCSP(cloudNumber);
		if(cloudNames.length>0){
		System.out.println("Cloud transferred to new CSP");
		}
		
	}
}
