package net.respectnetwork.sdk.csp.example;

import net.respectnetwork.sdk.csp.BasicCSPInformation;
import net.respectnetwork.sdk.csp.ssl.TLSv1Support;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;
import xdi2.discovery.XDIDiscoveryClient;

public class CSPInformationOnexusOTE extends BasicCSPInformation {

	private static final long serialVersionUID = -1186435920408698291L;

	public CSPInformationOnexusOTE() {

		super(
				CloudNumber.create("[+]!:uuid:2cfdf2c1-23e6-4108-a99b-a7bbb7e99a1f"),
				"https://mycloud-ote.neustar.biz/onexus-registry",
				"https://mycloud-ote.neustar.biz/onexus-users/",
				"9f9nqOHt4Yly",
				null,
				CloudNumber.create("[+]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa"),
				"https://registration-stage.respectnetwork.net/registration",
				XDIAddress.create("([+]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa/#registrar)$do"),
				null,
				XDIDiscoveryClient.NEUSTAR_OTE_DISCOVERY_CLIENT
				);

		TLSv1Support.supportTLSv1();
	}
}
