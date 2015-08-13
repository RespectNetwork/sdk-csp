package net.respectnetwork.sdk.csp.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import net.respectnetwork.sdk.csp.BasicCSP;
import net.respectnetwork.sdk.csp.BasicCSPInformation;
import net.respectnetwork.sdk.csp.CSP;
import xdi2.core.syntax.CloudName;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;

public class ExampleCreateService {


	/* CHOOSE THE INDIVIDUAL's CLOUD NAME HERE */
	private static CloudName cloudName;

	/* CHOOSE THE CSPs SECRET TOKEN HERE */
	private static String secretToken = "mysecret";
	
	/* Service XDI */
	private static String serviceXDI;
	
	/* Service URI */
	private static String serviceURI;
	
	


	static {

		try {
			System.out.print("Service XDI: [default: <$https><$test1><$uri>] ");
            serviceXDI = new BufferedReader(new InputStreamReader(System.in)).readLine();
            if (serviceXDI.trim().isEmpty()) serviceXDI = "<$https><$test1><$uri>";
            if (serviceXDI == null) {
                System.err.println("Invalid Service XDI.");
                System.exit(0);
            }
            
            System.out.print("Service URI: ");
            serviceURI = new BufferedReader(new InputStreamReader(System.in)).readLine();
            if (serviceURI.trim().isEmpty()) serviceURI = "https://test1.testcsp.com/service1";

            if (serviceURI == null) {
                System.err.println("Invalid Service URI.");
                System.exit(0);
            }

		} catch (Exception ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static void main(String[] args) throws Exception {

		// Step 0: Set up CSP

		BasicCSPInformation cspInformation = new CSPInformationTestCSPOTE();

		cspInformation.retrieveCspSignaturePrivateKey();
		cspInformation.setRnCspSecretToken(null);

		CSP csp = new BasicCSP(cspInformation);
			
        // Get CloudNumber from CloudName
		CloudNumber cloudNumber = cspInformation.getCspCloudNumber();
		
		Map<XDIAddress, String> services = new HashMap<XDIAddress, String> ();
		services.put(XDIAddress.create(serviceXDI), serviceURI);
		String cspXDIEndPoint = cspInformation.getCspRegistryXdiEndpoint() + "graph";
		csp.setCloudServicesForCSPInCSP(cloudNumber, cspInformation.getCspSecretToken(), cspXDIEndPoint, services);

		System.out.println("Done registering " + serviceXDI + " for Cloud Number " + cloudNumber + ".");
	}
}