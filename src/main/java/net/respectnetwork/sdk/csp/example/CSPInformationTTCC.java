package net.respectnetwork.sdk.csp.example;

import net.respectnetwork.sdk.csp.BasicCSPInformation;
import xdi2.core.xri3.CloudNumber;

public class CSPInformationTTCC extends BasicCSPInformation {

	private static final long serialVersionUID = 2008719552959657341L;

	public CSPInformationTTCC() {

		super(
				CloudNumber.create("[@]!:uuid:e9449d30-b032-4ad0-89a3-b7498fbc731e"),
				"ofniruoynwo",
				"http://clouds.ownyourinfo.com:14440/ownyourinfo-registry",
				"http://clouds.ownyourinfo.com:14440/ownyourinfo-users/",
				CloudNumber.create("[@]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa"),
				"http://registration-dev.respectnetwork.net/registration"
				);
	}
}
