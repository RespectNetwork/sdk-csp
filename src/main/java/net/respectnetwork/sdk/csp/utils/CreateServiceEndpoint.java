package net.respectnetwork.sdk.csp.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import net.respectnetwork.sdk.csp.BasicCSP;
import net.respectnetwork.sdk.csp.BasicCSPInformation;
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.ssl.TLSv1Support;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;
import xdi2.core.xri3.XDI3Segment;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;

public class CreateServiceEndpoint
{

   public static void main(String[] args) throws Exception
   {

      TLSv1Support.supportTLSv1();

      System.out.print("Environment(ote/prod/prod-au :");
      String str = new BufferedReader(new InputStreamReader(System.in))
            .readLine();
      while (str != null
            && (str.equalsIgnoreCase("ote") || (str.equalsIgnoreCase("prod")) || (str
                  .equalsIgnoreCase("prod-au"))))
      {
         String env = str;
         System.out.print("CSP name (without +) :");
         String cspName = new BufferedReader(new InputStreamReader(System.in))
               .readLine();
         System.out.print("CSP password :");
         String cspPassword = new BufferedReader(new InputStreamReader(
               System.in)).readLine();
         System.out.println("CSP Password [" + cspPassword + "]");

         System.out.println("Env : " + env);
         System.out.println("CSP Name : " + cspName);
         System.out.print("Service XDI: [ex: <$https><$test1>] ");
         String serviceXDI = new BufferedReader(
               new InputStreamReader(System.in)).readLine();
         if (serviceXDI.trim().isEmpty())
         {
            System.out.print("No Service XDI!");
            System.exit(0);
         }

         System.out.print("Service URI: ");
         String serviceURI = new BufferedReader(
               new InputStreamReader(System.in)).readLine();
         if (serviceURI.trim().isEmpty())
         {
            System.out.print("No Service URI!");
            System.exit(0);
         }

         System.out.print("Continue ? Y/N :");
         String toContinue = new BufferedReader(
               new InputStreamReader(System.in)).readLine();
         if (!toContinue.equalsIgnoreCase("y"))
         {
            break;
         }
         // Step 0: Set up CSP

         CloudNumber cspCloudNumber = null;
         String cspRegistryURL = "";
         String cspUserURL = "";
         XDIDiscoveryClient discoveryClient = null;
         String registrationSvcURL = "";
         BasicCSPInformation cspInformation = null;
         if (env.equalsIgnoreCase("prod"))
         {
            discoveryClient = XDIDiscoveryClient.NEUSTAR_PROD_DISCOVERY_CLIENT;
            cspRegistryURL = "https://mycloud.neustar.biz/" + cspName
                  + "-registry";
            cspUserURL = "https://mycloud.neustar.biz/" + cspName + "-users/";
            registrationSvcURL = "https://registration.respectnetwork.net/registration";
         } else if (env.equalsIgnoreCase("ote"))
         {
            discoveryClient = XDIDiscoveryClient.NEUSTAR_OTE_DISCOVERY_CLIENT;
            cspRegistryURL = "https://mycloud-ote.neustar.biz/" + cspName
                  + "-registry";
            cspUserURL = "https://mycloud-ote.neustar.biz/" + cspName
                  + "-users/";
            registrationSvcURL = "https://registration-stage.respectnetwork.net/registration";
         } else if (env.equalsIgnoreCase("prod-au"))
         {
            discoveryClient = XDIDiscoveryClient.NEUSTAR_PROD_DISCOVERY_CLIENT;
            cspRegistryURL = "https://mycloud-au.neustar.biz/" + cspName
                  + "-registry";
            ;
            cspUserURL = "https://mycloud-au.neustar.biz/" + cspName
                  + "-users/";
            registrationSvcURL = "https://registration.respectnetwork.net/registration";
         }
         System.out.println("CSP Resistry URL:" + cspRegistryURL);
         System.out.println("CSP User URL:" + cspUserURL);
         System.out.println("Regn. svc. URL:" + registrationSvcURL);
         System.out.println("Discovery info:" + discoveryClient.toString());
         try
         {
            XDIDiscoveryResult discResult = discoveryClient.discover(
                  XDI3Segment.create("+" + cspName), null);

            cspCloudNumber = discResult.getCloudNumber();
            System.out.println("CSP Cloud Number : "
                  + cspCloudNumber.toString());

         } catch (Xdi2ClientException e)
         {
            System.out.println("Xdi2ClientException: " + e.getMessage());
            e.printStackTrace();
         }
         cspInformation = new BasicCSPInformation(
               cspCloudNumber,
               cspRegistryURL,
               cspUserURL,
               cspPassword,
               null,
               CloudNumber
                     .create("[+]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa"),
               registrationSvcURL,
               XDI3Segment
                     .create("([+]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa/#registrar)$do"),
               null, discoveryClient);

         System.out.println("CSP Information:\n" + cspInformation.toString());
         cspInformation.setRnCspSecretToken(null);
         try
         {
            cspInformation.retrieveCspSignaturePrivateKey();
         } catch (Xdi2ClientException e2)
         {
            // TODO Auto-generated catch block
            e2.printStackTrace();
         } catch (GeneralSecurityException e2)
         {
            // TODO Auto-generated catch block
            e2.printStackTrace();
         }
         if (cspInformation.getCspSignaturePrivateKey() == null)
         {
            System.err.println("Invalid CSP Information.");
            System.exit(0);
         }
         CSP csp = new BasicCSP(cspInformation);

         // Get CloudNumber from CloudName
         CloudNumber cloudNumber = cspInformation.getCspCloudNumber();

         Map<XDI3Segment, String> services = new HashMap<XDI3Segment, String>();
         services.put(XDI3Segment.create(serviceXDI), serviceURI);
         String cspXDIEndPoint = cspInformation.getCspRegistryXdiEndpoint()
               + "graph";
         csp.setCloudServicesForCSPInCSP(cloudNumber,
               cspInformation.getCspSecretToken(), cspXDIEndPoint, services);

         System.out.println("Done registering " + serviceXDI
               + " for Cloud Number " + cloudNumber + ".");

         System.out.print("Create another ? Y/N :");
         toContinue = new BufferedReader(new InputStreamReader(System.in))
               .readLine();
         if (!toContinue.equalsIgnoreCase("y"))
         {
            break;
         }
      }

   }

}
