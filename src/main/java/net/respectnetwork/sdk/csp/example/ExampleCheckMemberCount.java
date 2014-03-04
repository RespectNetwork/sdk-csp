package net.respectnetwork.sdk.csp.example;

import net.respectnetwork.sdk.csp.BasicCSP;
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.CSPInformation;
import xdi2.client.exceptions.Xdi2ClientException;

public class ExampleCheckMemberCount {



	public static void main(String[] args) throws Xdi2ClientException {


		CSPInformation cspInformation = new CSPInformationRespectNetworkOTE();

		CSP csp = new BasicCSP(cspInformation);
        
		try {
		    //long members = csp.getRespectFirstMemberCount(cspInformation.getCspSecretToken());
	        long members = csp.getRespectFirstMemberCount();

		    System.out.println("Number of Respect First Members = " + members );

		} catch (Xdi2ClientException e) {
		    System.out.println("Exception calling getRespectFirstMemberCount");
		    e.printStackTrace();
		}

	}
}
