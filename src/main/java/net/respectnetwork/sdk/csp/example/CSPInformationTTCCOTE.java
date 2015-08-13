package net.respectnetwork.sdk.csp.example;

import net.respectnetwork.sdk.csp.BasicCSPInformation;
import net.respectnetwork.sdk.csp.ssl.TLSv1Support;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;
import xdi2.discovery.XDIDiscoveryClient;

public class CSPInformationTTCCOTE extends BasicCSPInformation {

	private static final long serialVersionUID = -1186435920408698291L;

	public CSPInformationTTCCOTE() {

		super(                      
				CloudNumber.create("[+]!:uuid:e9449d30-b032-4ad0-89a3-b7498fbc731e"),
				"https://mycloud-ote.neustar.biz/ownyourinfo-registry",
				"https://mycloud-ote.neustar.biz/ownyourinfo-users/",
				"ofniruoynwo",
				null,
				CloudNumber.create("[+]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa"),
				"http://registration-dev.respectnetwork.net/registration",
				XDIAddress.create("([+]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa/#registrar)$do"),
				null,
				XDIDiscoveryClient.NEUSTAR_OTE_DISCOVERY_CLIENT
				);

		TLSv1Support.supportTLSv1();
	}
}
