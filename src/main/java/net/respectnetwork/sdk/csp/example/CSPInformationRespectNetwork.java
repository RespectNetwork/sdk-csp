package net.respectnetwork.sdk.csp.example;

import net.respectnetwork.sdk.csp.BasicCSPInformation;
import xdi2.core.xri3.CloudNumber;

public class CSPInformationRespectNetwork extends BasicCSPInformation {

	private static final long serialVersionUID = -1186435920408698291L;

	public CSPInformationRespectNetwork() {

		super(
				CloudNumber.create("[@]!:uuid:299089fd-9d81-3c59-2990-89fd9d813c59"),
				"s3cr3t",
				"http://mycloud.neustar.biz:14440/registry",
				"http://mycloud.neustar.biz:14440/users/"
				);
	}
}
