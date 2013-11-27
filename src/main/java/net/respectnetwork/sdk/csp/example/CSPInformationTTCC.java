package net.respectnetwork.sdk.csp.example;

import net.respectnetwork.sdk.csp.BasicCSPInformation;
import xdi2.core.xri3.CloudNumber;

public class CSPInformationTTCC extends BasicCSPInformation {

	private static final long serialVersionUID = 2008719552959657341L;

	public CSPInformationTTCC() {

		super(
				CloudNumber.create("[@]!:uuid:f34559e4-6b2b-d962-f345-59e46b2bd962"),
				"ofniruoynwo",
				"http://clouds.ownyourinfo.com:14440/ownyourinfo-registry",
				"http://clouds.ownyourinfo.com:14440/ownyourinfo-users/"
				);
	}
}
