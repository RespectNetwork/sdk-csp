package net.respectnetwork.sdk.csp.example;

import net.respectnetwork.sdk.csp.BasicCSPInformation;
import net.respectnetwork.sdk.csp.ssl.TLSv1Support;
import xdi2.core.xri3.CloudNumber;
import xdi2.core.xri3.XDI3Segment;
import xdi2.discovery.XDIDiscoveryClient;

public class CSPInformationEmmettStage extends BasicCSPInformation {

	private static final long serialVersionUID = -1186435920408698291L;

	public CSPInformationEmmettStage() {

		super(
				CloudNumber.create("[+]!:uuid:690404d0-df12-4a60-a98c-fd975ea79e5b"),
				"https://mycloud-stg.cloudnames.biz/emmettglobal-registry",
				"https://mycloud-stg.cloudnames.biz/emmettglobal-users/",
				"P3hpJ0ErjqIq",
				null,
				CloudNumber.create("[+]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa"),
				"http://registration-stage.respectnetwork.net/registration",
				XDI3Segment.create("([+]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa/#registrar)$do"),
				null,
				XDIDiscoveryClient.NEUSTAR_OTE_DISCOVERY_CLIENT
				);

		TLSv1Support.supportTLSv1();
	}
}
