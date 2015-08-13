package net.respectnetwork.sdk.csp.example;

import net.respectnetwork.sdk.csp.BasicCSPInformation;
import net.respectnetwork.sdk.csp.ssl.TLSv1Support;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;
import xdi2.discovery.XDIDiscoveryClient;

public class CSPInformationRespectNetworkPROD extends BasicCSPInformation {

	private static final long serialVersionUID = -1186435920408698291L;

	public CSPInformationRespectNetworkPROD() {

		super(
				CloudNumber.create("[+]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa"),
				"https://mycloud.neustar.biz/registry",
				"https://mycloud.neustar.biz/users/",
				"s3cr3t",
				null,
				CloudNumber.create("[+]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa"),
				"https://registration.respectnetwork.net/registration",
				XDIAddress.create("([+]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa/#registrar)$do"),
				"s3cr3t",
				XDIDiscoveryClient.NEUSTAR_PROD_DISCOVERY_CLIENT
				);

		TLSv1Support.supportTLSv1();
	}
}
