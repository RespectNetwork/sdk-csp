package net.respectnetwork.sdk.csp.example;

import net.respectnetwork.sdk.csp.BasicCSPInformation;
import xdi2.core.xri3.CloudNumber;
import xdi2.core.xri3.XDI3Segment;

public class CSPInformationTTCC extends BasicCSPInformation {

	private static final long serialVersionUID = 2008719552959657341L;

	public CSPInformationTTCC() {

		super(
				CloudNumber.create("[@]!:uuid:e9449d30-b032-4ad0-89a3-b7498fbc731e"),
				"http://clouds.ownyourinfo.com:14440/ownyourinfo-registry",
				"http://clouds.ownyourinfo.com:14440/ownyourinfo-users/",
				"ofniruoynwo",
				CloudNumber.create("[@]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa"),
				"https://registration.respectnetwork.net/registration",
				XDI3Segment.create("[@]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa$to+registrar$from$do"),
				"ofniruoynwo"
				);
	}
}
