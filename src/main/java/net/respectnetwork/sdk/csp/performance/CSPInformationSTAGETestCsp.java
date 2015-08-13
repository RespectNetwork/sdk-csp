package net.respectnetwork.sdk.csp.performance;

import net.respectnetwork.sdk.csp.BasicCSPInformation;
import net.respectnetwork.sdk.csp.ssl.TLSv1Support;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;
import xdi2.discovery.XDIDiscoveryClient;

public class CSPInformationSTAGETestCsp extends BasicCSPInformation {

	private static final long serialVersionUID = -1186435920408698291L;

	public CSPInformationSTAGETestCsp() {

		super(
				CloudNumber.create("[+]!:uuid:20e3d77c-5a0e-44d5-b949-85153fbb6577"),
				"https://mycloud-ote.neustar.biz/testcsp-registry",
				"https://mycloud-ote.neustar.biz/testcsp-users/",
				"whitelabel123",
				null,
				CloudNumber.create("[+]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa"),
				"https://registration-stage.respectnetwork.net/registration/graph",
				XDIAddress.create("([+]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa/#registrar)$do"),
				"testcsp",
				XDIDiscoveryClient.NEUSTAR_OTE_DISCOVERY_CLIENT
				);

		TLSv1Support.supportTLSv1();
	}
}
