package net.respectnetwork.sdk.csp.example;

import net.respectnetwork.sdk.csp.BasicCSP;
import net.respectnetwork.sdk.csp.BasicCSPInformation;
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.CSPInformation;
import net.respectnetwork.sdk.csp.CloudNameRegistration;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.xri3.XDI3Segment;

public class ExampleChangeSecretToken {

	/* CHOOSE THE INDIVIDUAL's CLOUD NAME HERE */
	private static XDI3Segment cloudName = XDI3Segment.create("=dev.test.531");

	/* CHOOSE THE INDIVIDUAL's SECRET TOKEN HERE */
	private static String secretToken = "mynewsecret";

	public static void main(String[] args) throws Xdi2ClientException {

		CloudNameRegistration cloudNameRegistration;

		// step 0: Set up CSP object

		CSPInformation cspInformation = makeCSPInformationNeustar();
		//CSPInformation cspInformation = makeCSPInformationTTCC();

		CSP csp = new BasicCSP(cspInformation);

		// step 1: Look for registered Cloud Name

		cloudNameRegistration = csp.checkCloudNameAvailable(cloudName);
		if (cloudNameRegistration == null) throw new RuntimeException("Cloud Name " + cloudName + " does not exist.");

		// step 2: Change Secret Token

		csp.setCloudSecretToken(cloudNameRegistration, secretToken);

		// done

		System.out.println("Done changing secret token for Cloud Name " + cloudName);
	}

	static CSPInformation makeCSPInformationNeustar() {

		XDI3Segment cspCloudNumber = XDI3Segment.create("[@]!:uuid:0baea650-823b-2475-0bae-a650823b2475");
		String cspSecretToken = "s3cr3t";
		String hostingEnvironmentRegistryXdiEndpoint = "http://mycloud.neustar.biz:14440/registry";
		String hostingEnvironmentCloudBaseXdiEndpoint = "http://mycloud.neustar.biz:14440/users/";

		return new BasicCSPInformation(cspCloudNumber, cspSecretToken, hostingEnvironmentRegistryXdiEndpoint, hostingEnvironmentCloudBaseXdiEndpoint);
	}

	static CSPInformation makeCSPInformationTTCC() {

		XDI3Segment cspCloudNumber = XDI3Segment.create("[@]!:uuid:f34559e4-6b2b-d962-f345-59e46b2bd962");
		String cspSecretToken = "ofniruoynwo";
		String hostingEnvironmentRegistryXdiEndpoint = "http://clouds.ownyourinfo.com:14440/ownyourinfo-registry";
		String hostingEnvironmentCloudBaseXdiEndpoint = "http://clouds.ownyourinfo.com:14440/ownyourinfo-users/";

		return new BasicCSPInformation(cspCloudNumber, cspSecretToken, hostingEnvironmentRegistryXdiEndpoint, hostingEnvironmentCloudBaseXdiEndpoint);
	}
}
