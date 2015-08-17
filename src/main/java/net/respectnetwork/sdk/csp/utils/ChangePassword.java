package net.respectnetwork.sdk.csp.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.respectnetwork.sdk.csp.BasicCSP;
import net.respectnetwork.sdk.csp.BasicCSPInformation;
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.discount.NeustarRNDiscountCode;
import net.respectnetwork.sdk.csp.discount.RespectNetworkRNDiscountCode;
import net.respectnetwork.sdk.csp.exception.CSPRegistrationException;
import net.respectnetwork.sdk.csp.ssl.TLSv1Support;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.syntax.CloudName;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;

public class ChangePassword
{

   /* CHOOSE THE INDIVIDUAL's CLOUD NUMBER HERE */
   private CloudNumber cloudNumber;

   /* CHOOSE THE INDIVIDUAL's CLOUD NAME HERE */
   private CloudName   cloudName;

   public void changePassword(String env, String cspName, String cspPassword,
         String cloudNameString, String currentPassword, String newPassword)
   {
      // Step 0: Set up CSP

      CloudNumber cspCloudNumber = null;
      String cspRegistryURL = "";
      String cspUserURL = "";
      XDIDiscoveryClient discoveryClient = null;
      String registrationSvcURL = "";

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
         cspUserURL = "https://mycloud-ote.neustar.biz/" + cspName + "-users/";
         registrationSvcURL = "https://registration-stage.respectnetwork.net/registration";
      } else if (env.equalsIgnoreCase("prod-au"))
      {
         discoveryClient = XDIDiscoveryClient.NEUSTAR_PROD_DISCOVERY_CLIENT;
         cspRegistryURL = "https://mycloud-au.neustar.biz/" + cspName
               + "-registry";
         ;
         cspUserURL = "https://mycloud-au.neustar.biz/" + cspName + "-users/";
         registrationSvcURL = "https://registration.respectnetwork.net/registration";
      }
      System.out.println("CSP Resistry URL:" + cspRegistryURL);
      System.out.println("CSP User URL:" + cspUserURL);
      System.out.println("Regn. svc. URL:" + registrationSvcURL);
      System.out.println("Discovery info:" + discoveryClient.toString());
      try
      {
         XDIDiscoveryResult discResult = discoveryClient.discover(
               XDIAddress.create("+" + cspName), null);

         cspCloudNumber = discResult.getCloudNumber();
         System.out.println("CSP Cloud Number : " + cspCloudNumber.toString());

      } catch (Xdi2ClientException e)
      {
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

      cloudName = CloudName.create(cloudNameString);

      if (cloudName == null || !cloudNameString.startsWith("="))
      {

         System.err.println("Invalid Cloud Name.");
         System.exit(0);
      }
      try
      {
         XDIDiscoveryResult discResult = discoveryClient.discover(
               XDIAddress.create(cloudNameString), null);

         cloudNumber = discResult.getCloudNumber();
         System.out.println("Cloud Number : " + cloudNumber.toString());

      } catch (Xdi2ClientException e)
      {
         System.out.println("Xdi2ClientException: " + e.getMessage());
         e.printStackTrace();
      }
      try
      {

         CSP csp = new BasicCSP(cspInformation);

         // Step 1: authenticate user

         try
         {
            csp.authenticateInCloud(cloudNumber, currentPassword);
         } catch (Xdi2ClientException badpass)
         {
            System.out.println("Cannot authenticate. Exiting ...");
            System.exit(0);
         }

         // Step 2: Change Secret Token

         csp.setCloudSecretTokenInCSP(cloudNumber, newPassword);

         // done

         System.out.println("Done Changing password for cloudname "
               + cloudNameString);
      } catch (RuntimeException ex1)
      {
         ex1.printStackTrace();
      } catch (Xdi2ClientException ex2)
      {
         ex2.printStackTrace();
      }
   }

   public static void main(String[] args) throws Exception
   {
      TLSv1Support.supportTLSv1();

      System.out.print("Environment(ote/prod/prod-au) :");
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
         System.out.print("CloudName :");
         String cloudNameString = new BufferedReader(new InputStreamReader(
               System.in)).readLine();
         
         System.out.print("Cloud password :");
         String cloudPass = new BufferedReader(new InputStreamReader(System.in))
               .readLine();
         
         System.out.print("New password :");
         String newPass = new BufferedReader(new InputStreamReader(System.in))
               .readLine();
         
         System.out.println("Changing password with this information :");
         System.out.println("Env : " + env);
         System.out.println("CSP Name : " + cspName);
         System.out.println("Cloud Name: " + cloudNameString);
         
         System.out.print("Continue ? Y/N :");
         String toContinue = new BufferedReader(
               new InputStreamReader(System.in)).readLine();
         if (!toContinue.equalsIgnoreCase("y"))
         {
            break;
         }
         ChangePassword cp = new ChangePassword();
         cp.changePassword(env, cspName, cspPassword, cloudNameString, cloudPass, newPass);
         System.out.print("Do another ? Y/N :");
         toContinue = new BufferedReader(new InputStreamReader(System.in))
               .readLine();
         if (!toContinue.equalsIgnoreCase("y"))
         {
            break;
         }
      }

   }
}
