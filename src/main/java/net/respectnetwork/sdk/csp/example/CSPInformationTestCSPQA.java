package net.respectnetwork.sdk.csp.example;

import net.respectnetwork.sdk.csp.BasicCSPInformation;
import net.respectnetwork.sdk.csp.ssl.TLSv1Support;
import xdi2.client.impl.http.XDIHttpClient;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;
import xdi2.discovery.XDIDiscoveryClient;

public class CSPInformationTestCSPQA extends BasicCSPInformation {

    private static final long serialVersionUID = -1186435920408698291L;

    public CSPInformationTestCSPQA() {

        super(CloudNumber.create("+!:uuid:20e3d77c-5a0e-44d5-b949-85153fbb6577"),
                "https://shared-qa-mycloud.respectnetwork.net/testcsp-registry",
                "https://shared-qa-mycloud.respectnetwork.net/testcsp-users/", "whitelabel123", null, CloudNumber
                        .create("+!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa"),
                "http://shared-qa-registry.respectnetwork.net:3081/registry", XDIAddress
                        .create("(+!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa/#registrar)$do"), null,
                new XDIDiscoveryClient(new XDIHttpClient("http://shared-qa-registry.respectnetwork.net:3081/registry")));

        TLSv1Support.supportTLSv1();
    }
}
