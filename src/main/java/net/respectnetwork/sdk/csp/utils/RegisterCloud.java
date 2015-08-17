/**
 * 
 */
package net.respectnetwork.sdk.csp.utils;

import java.security.GeneralSecurityException;
import java.util.UUID;

import net.respectnetwork.sdk.csp.BasicCSP;
import net.respectnetwork.sdk.csp.BasicCSPInformation;
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.discount.NeustarRNDiscountCode;
import net.respectnetwork.sdk.csp.discount.RespectNetworkRNDiscountCode;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.http.XDIHttpClient;
import xdi2.core.syntax.CloudName;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;

/**
 * @author kvats
 * 
 */
public abstract class RegisterCloud {

    /* CHOOSE THE INDIVIDUAL's CLOUD NUMBER HERE */
    public static CloudNumber cloudNumber;

    /* CHOOSE THE INDIVIDUAL's CLOUD NAME HERE */
    public static CloudName cloudName;

    /* CHOOSE A CLOUD NAME DISCOUNT CODE */
    public static NeustarRNDiscountCode cloudNameDiscountCode = NeustarRNDiscountCode.OnePersonOneName;

    /* CHOOSE A RESPECT NETWORK MEMBERSHIP DISCOUNT CODE */
    public static RespectNetworkRNDiscountCode respectNetworkMembershipDiscountCode = RespectNetworkRNDiscountCode.IIW17;

    public static CloudNumber cspCloudNumber = null;
    public static String cspRegistryURL = "";
    public static String cspUserURL = "";
    public static XDIDiscoveryClient discoveryClient = null;
    public static String registrationSvcURL = "";
    public static CSP csp = null;

    public void setup(String env, String cspName, String cspPassword,
            String cloudNameString, String cloudNumberString) {
        if (env.equalsIgnoreCase("prod")) {
            discoveryClient = XDIDiscoveryClient.NEUSTAR_PROD_DISCOVERY_CLIENT;
            cspRegistryURL = "https://mycloud.neustar.biz/" + cspName
                    + "-registry";
            cspUserURL = "https://mycloud.neustar.biz/" + cspName + "-users/";
            registrationSvcURL = "https://registration.respectnetwork.net/registration";
        } else if (env.equalsIgnoreCase("ote")) {
            discoveryClient = XDIDiscoveryClient.NEUSTAR_OTE_DISCOVERY_CLIENT;
            cspRegistryURL = "https://mycloud-ote.neustar.biz/" + cspName
                    + "-registry";
            cspUserURL = "https://mycloud-ote.neustar.biz/" + cspName
                    + "-users/";
            registrationSvcURL = "https://registration-ote.respectnetwork.net/registration";
        } else if (env.equalsIgnoreCase("prod-au")) {
            discoveryClient = XDIDiscoveryClient.NEUSTAR_PROD_DISCOVERY_CLIENT;
            cspRegistryURL = "https://mycloud-au.neustar.biz/" + cspName
                    + "-registry";
            ;
            cspUserURL = "https://mycloud-au.neustar.biz/" + cspName
                    + "-users/";
            registrationSvcURL = "https://registration.respectnetwork.net/registration";
        } else if (env.equalsIgnoreCase("stg")) {
            discoveryClient = new XDIDiscoveryClient(new XDIHttpClient(
                    "https://xdidiscovery-stg.cloudnames.biz"));
            cspRegistryURL = "https://mycloud-stg.cloudnames.biz/" + cspName
                    + "-registry";
            cspUserURL = "https://mycloud-stg.cloudnames.biz/" + cspName
                    + "-users/";
            registrationSvcURL = "https://registration-stage.respectnetwork.net/registration";
        }
        System.out.println("CSP Resistry URL:" + cspRegistryURL);
        System.out.println("CSP User URL:" + cspUserURL);
        System.out.println("Regn. svc. URL:" + registrationSvcURL);
        System.out.println("Discovery info:" + discoveryClient.toString());
        try {
            XDIDiscoveryResult discResult = discoveryClient.discover(
                    XDIAddress.create("+" + cspName), null);

            cspCloudNumber = discResult.getCloudNumber();
            System.out.println("CSP Cloud Number : "
                    + cspCloudNumber.toString());

        } catch (Xdi2ClientException e) {
            System.out.println("Xdi2ClientException: " + e.getMessage());
            e.printStackTrace();
        }
        BasicCSPInformation cspInformation = new BasicCSPInformation(
                cspCloudNumber,
                cspRegistryURL,
                cspUserURL,
                cspPassword,
                null,
                CloudNumber
                        .create("[+]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa"),
                registrationSvcURL,
                XDIAddress
                        .create("([+]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa/#registrar)$do"),
                null, discoveryClient);

        System.out.println("CSP Information:\n" + cspInformation.toString());
        cspInformation.setRnCspSecretToken(null);
        try {
            cspInformation.retrieveCspSignaturePrivateKey();
        } catch (Xdi2ClientException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        } catch (GeneralSecurityException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        if (cspInformation.getCspSignaturePrivateKey() == null) {
            System.err.println("Invalid CSP Information.");
            System.exit(0);
        }

        cloudName = CloudName.create(cloudNameString);

        if (cloudName == null || !cloudNameString.startsWith("=")) {

            System.err.println("Invalid Cloud Name.");
            System.exit(0);
        }
        if (cloudNumberString == null || cloudNumberString.isEmpty())
            cloudNumberString = "[=]!:uuid:" + UUID.randomUUID().toString();

        cloudNumber = CloudNumber.create(cloudNumberString);
        if (cloudName == null) {

            System.err.println("Invalid Cloud Number.");
            System.exit(0);
        }
        csp = new BasicCSP(cspInformation);
    }

    public abstract void register(String cloudNameString, String secretToken,
            String dependentBirthDate, String guardianCloudName,
            String guardianPassword, String verifiedPhone,
            String verifiedEmail, String step);

}
