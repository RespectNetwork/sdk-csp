/**
 * 
 */
package net.respectnetwork.sdk.csp.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.respectnetwork.sdk.csp.BasicCSPInformation;
import net.respectnetwork.sdk.csp.ssl.TLSv1Support;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.util.XDIClientUtil;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;

/**
 * @author kvats
 * 
 */
public class RegisterDependentCloud extends RegisterCloud {

    public void register(String cloudNameString, String dependentToken,
            String birthDate, String guardianCloudName, String guardianToken,
            String verifiedPhone, String verifiedEmail, String step) {
        // Common Data

        CloudNumber guardianCloudNumber = null;
        CloudNumber dependentCloudNumber = null;
        PrivateKey guardianPrivateKey = null;
        PrivateKey dependentPrivateKey = null;
        boolean withConsent = true;

        Date dependentBirthDate = null;

        BasicCSPInformation cspInformation = (BasicCSPInformation) csp
                .getCspInformation();

        // Resolve Cloud Numbers from Name

        XDIDiscoveryClient discovery = cspInformation.getXdiDiscoveryClient();

        try {
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
            dependentBirthDate = format.parse(birthDate);
        } catch (ParseException e) {
            System.out.println("Invalid Dependent BirthDate.");
        }

        discovery.setAuthorityCache(null);
   //     discovery.setRegistryCache(null);

        for (int tries = 0; tries < 10; tries++) {

            try {
                System.out
                        .println("Waiting for five seconds to allow for the newly registered dependent name in discovery");
                Thread.sleep(5000);
            } catch (InterruptedException e1) {

            }

            try {

                XDIDiscoveryResult guardianRegistry = discovery
                        .discoverFromRegistry(XDIAddress
                                .create(guardianCloudName.toString()), null);

                XDIDiscoveryResult dependentRegistry = discovery
                        .discoverFromRegistry(
                                XDIAddress.create(cloudNameString), null);
                if (dependentRegistry == null
                        || dependentRegistry.getCloudNumber() == null) {
                    System.out
                            .println("Dependent name is not in discovery yet. So going back to check again ...");
                    continue;
                }

                guardianCloudNumber = guardianRegistry.getCloudNumber();
                dependentCloudNumber = dependentRegistry.getCloudNumber();

                URL guardianXdiEndpoint = guardianRegistry
                        .getXdiEndpointUrl();
                URL dependentXdiEndpoint = dependentRegistry
                        .getXdiEndpointUrl();

                guardianPrivateKey = XDIClientUtil
                        .retrieveSignaturePrivateKey(guardianCloudNumber,
                                guardianXdiEndpoint, guardianToken);
                System.out.println("GuardianPrivateKey Algo: "
                        + guardianPrivateKey.getAlgorithm());

                dependentPrivateKey = XDIClientUtil
                        .retrieveSignaturePrivateKey(dependentCloudNumber,
                                dependentXdiEndpoint, dependentToken);
                System.out.println("DependentPrivateKey Algo: "
                        + dependentPrivateKey.getAlgorithm());

                if (guardianCloudNumber == null || dependentCloudNumber == null) {
                    System.out.println("Un-registered Cloud Name.");
                    continue;
                }
                break;

            } catch (Xdi2ClientException e) {
                System.out.println("Problem with Cloud Name Provided.");
                e.printStackTrace();
                System.out.println(e.getMessage());
                continue;
            } catch (GeneralSecurityException gse) {
                System.out.println("Problem retrieving signatures.");
                gse.printStackTrace();
                System.out.println(gse.getMessage());
                continue;
            }

        }
        if (guardianCloudNumber != null && dependentCloudNumber != null) {
            try {
                // Set User Cloud Data
                csp.setGuardianshipInCloud(cspInformation, guardianCloudNumber,
                        dependentCloudNumber, dependentBirthDate, withConsent,
                        guardianToken, guardianPrivateKey, dependentToken);

                // Set CSP Cloud Data
                csp.setGuardianshipInCSP(cspInformation, guardianCloudNumber,
                        dependentCloudNumber, dependentBirthDate, withConsent,
                        guardianPrivateKey);

                // Set MemberGraph Data
                csp.setGuardianshipInRN(cspInformation, guardianCloudNumber,
                        dependentCloudNumber, dependentBirthDate, withConsent,
                        guardianPrivateKey);

            } catch (Xdi2ClientException e) {
                System.out.println("Xdi2ClientException: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        TLSv1Support.supportTLSv1();

        System.out.print("Environment(stg/ote/prod/prod-au ):");
        String str = new BufferedReader(new InputStreamReader(System.in))
                .readLine();
        while (str != null
                && (str.equalsIgnoreCase("stg") || str.equalsIgnoreCase("ote")
                        || (str.equalsIgnoreCase("prod")) || (str
                            .equalsIgnoreCase("prod-au")))) {
            String env = str;
            System.out.print("CSP name (without +) :");
            String cspName = new BufferedReader(
                    new InputStreamReader(System.in)).readLine();
            System.out.print("CSP password :");
            String cspPassword = new BufferedReader(new InputStreamReader(
                    System.in)).readLine();
            System.out.println("CSP Password [" + cspPassword + "]");
            System.out.print("Dependent CloudName (with =):");
            String cloudNameString = new BufferedReader(new InputStreamReader(
                    System.in)).readLine();
            System.out.print("Dependent CloudNumber :");
            String cloudNumberString = new BufferedReader(
                    new InputStreamReader(System.in)).readLine();
            System.out.print("Dependent Cloud password :");
            String cloudPass = new BufferedReader(new InputStreamReader(
                    System.in)).readLine();
            System.out.print("Dependent birth date(mm/dd/yyyy):");
            String dependentBirthDate = new BufferedReader(
                    new InputStreamReader(System.in)).readLine();
            System.out.print("Guardian CloudName :");
            String guardianCloudName = new BufferedReader(
                    new InputStreamReader(System.in)).readLine();
            System.out.print("Guardian Password :");
            String guardianPassword = new BufferedReader(new InputStreamReader(
                    System.in)).readLine();
            System.out.print("Step number :");
            String step = new BufferedReader(new InputStreamReader(System.in))
                    .readLine();
            System.out.println("Creating cloudname with this information :");
            System.out.println("Env : " + env);
            System.out.println("CSP Name : " + cspName);
            System.out.println("Cloud Name: " + cloudNameString);
            System.out.println("Cloud Number:" + cloudNumberString);
            System.out.println("Dependent birth date: " + dependentBirthDate);
            System.out.println("Guardian CloudName : " + guardianCloudName);
            System.out.println("Guardian Password :" + guardianPassword);
            System.out.println("Step :" + step);
            System.out.print("Continue ? Y/N :");
            String toContinue = new BufferedReader(new InputStreamReader(
                    System.in)).readLine();
            if (!toContinue.equalsIgnoreCase("y")) {
                break;
            }
            RegisterCloud regMachine = new RegisterDependentCloud();
            regMachine.setup(env, cspName, cspPassword, cloudNameString,
                    cloudNumberString);

            RegisterCloudName registerCloud = new RegisterCloudName();
            registerCloud.register(cloudNameString, cloudPass,
                    dependentBirthDate, guardianCloudName, guardianPassword,
                    " ", " ", step);

            regMachine.register(cloudNameString, cloudPass, dependentBirthDate,
                    guardianCloudName, guardianPassword, null, null, step);
            System.out.print("Create another ? Y/N :");
            toContinue = new BufferedReader(new InputStreamReader(System.in))
                    .readLine();
            if (!toContinue.equalsIgnoreCase("y")) {
                break;
            }
        }
    }
}
