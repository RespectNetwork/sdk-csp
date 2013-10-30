package net.respectnetwork.sdk.csp.example;

import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.CloudNameRegistration;
import net.respectnetwork.sdk.csp.CloudRegistration;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.xri3.XDI3Segment;

public class ExampleRegister {

	/* CHOOSE THE INDIVIDUAL's CLOUD NAME HERE */
	private static XDI3Segment cloudName = XDI3Segment.create("=dev.test.525");

	/* CHOOSE THE INDIVIDUAL's SECRET TOKEN HERE */
	private static String secretToken = "mysecret";

	public static void main(String[] args) throws Xdi2ClientException {

		CloudNameRegistration cloudNameRegistration;
		CloudRegistration cloudRegistration;

		// step 0: Set up CSP object

		CSP csp = makeCSPNeustar();
		//CSP csp = makeCSPTTCC();

		// step 1: Check if Cloud Name available

		cloudNameRegistration = csp.checkCloudNameAvailable(cloudName);
		if (cloudNameRegistration != null) throw new RuntimeException("Cloud Name " + cloudName + " is already registered.");

		// step 2: Register Cloud Name

		cloudNameRegistration = csp.registerCloudName(cloudName);
		if (cloudNameRegistration == null) throw new RuntimeException("Cloud Name " + cloudName + " could not be registered.");

		// step 3: Register Cloud with Cloud Number and Shared Secret

		cloudRegistration = csp.registerCloud(cloudNameRegistration, secretToken);
		if (cloudRegistration == null) throw new RuntimeException("Cloud " + cloudName + " could not be registered.");

		// step 4: Register Cloud XDI URL with Cloud Number

		csp.registerCloudXdiUrl(cloudNameRegistration, cloudRegistration);

		// done

		System.out.println("Done registering Cloud Name " + cloudName);
	}

	static CSP makeCSPNeustar() {

		XDI3Segment cspCloudNumber = XDI3Segment.create("[@]!:uuid:0baea650-823b-2475-0bae-a650823b2475");
		String cspSecretToken = "s3cr3t";
		String hostingEnvironmentRegistryXdiEndpoint = "http://mycloud.neustar.biz:14440/registry";
		String hostingEnvironmentCloudBaseXdiEndpoint = "http://mycloud.neustar.biz:14440/users/";

		return new CSP(cspCloudNumber, cspSecretToken, hostingEnvironmentRegistryXdiEndpoint, hostingEnvironmentCloudBaseXdiEndpoint);
	}

	static CSP makeCSPTTCC() {

		XDI3Segment cspCloudNumber = XDI3Segment.create("[@]!:uuid:f34559e4-6b2b-d962-f345-59e46b2bd962");
		String cspSecretToken = "ofniruoynwo";
		String hostingEnvironmentRegistryXdiEndpoint = "http://clouds.ownyourinfo.com:14440/ownyourinfo-registry";
		String hostingEnvironmentCloudBaseXdiEndpoint = "http://clouds.ownyourinfo.com:14440/ownyourinfo-users/";

		return new CSP(cspCloudNumber, cspSecretToken, hostingEnvironmentRegistryXdiEndpoint, hostingEnvironmentCloudBaseXdiEndpoint);
	}
}
