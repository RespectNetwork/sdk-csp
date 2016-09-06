package net.respectnetwork.sdk.csp.example;

import net.respectnetwork.sdk.csp.BasicCSPInformation;
import net.respectnetwork.sdk.csp.ssl.TLSv1Support;
import xdi2.client.impl.http.XDIHttpClient;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;
import xdi2.discovery.XDIDiscoveryClient;

public class CSPInformationTestCSPSandbox extends BasicCSPInformation {

    private static final long serialVersionUID = -1186435920408698291L;

    public CSPInformationTestCSPSandbox() {

        super(CloudNumber.create("<app dev cloud number>"), "<sandbox registry graph url>",
                "<sandbox users graph url>", "app dev password", null, CloudNumber.create("<RN cloud number>"),
                "<sandbox registry url>", XDIAddress.create("(<RN cloud number>/#registrar)$do"), null,
                new XDIDiscoveryClient(new XDIHttpClient("<sandbox registry url>")));

        TLSv1Support.supportTLSv1();
    }
}
