package net.respectnetwork.sdk.csp.example;

import net.respectnetwork.sdk.csp.BasicCSPInformation;
import net.respectnetwork.sdk.csp.ssl.TLSv1Support;
import xdi2.client.impl.http.XDIHttpClient;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;
import xdi2.discovery.XDIDiscoveryClient;

public class CSPInformationTestCSPDev extends BasicCSPInformation {

    private static final long serialVersionUID = -1186435920408698291L;

    public CSPInformationTestCSPDev() {

        super(CloudNumber.create("+!:uuid:20e3d77c-5a0e-44d5-b949-85153fbb6577"),
                "https://dev-mycloud.respectnetwork.net/testcsp-registry",
                "https://dev-mycloud.respectnetwork.net/testcsp-users/", "<*********>", null, CloudNumber
                        .create("+!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa"),
                "http://dev-registry.respectnetwork.net:3081/registry", XDIAddress
                        .create("(+!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa/#registrar)$do"), null,
                new XDIDiscoveryClient(new XDIHttpClient("http://dev-registry.respectnetwork.net:3081/registry")));

        TLSv1Support.supportTLSv1();
    }
}
