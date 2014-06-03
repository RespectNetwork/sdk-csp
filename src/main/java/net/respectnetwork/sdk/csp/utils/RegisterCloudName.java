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
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;
import xdi2.core.xri3.XDI3Segment;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;

public class RegisterCloudName
{

   /* CHOOSE THE INDIVIDUAL's CLOUD NUMBER HERE */
   private CloudNumber                                 cloudNumber;

   /* CHOOSE THE INDIVIDUAL's CLOUD NAME HERE */
   private CloudName                                   cloudName;

   
   /* CHOOSE A CLOUD NAME DISCOUNT CODE */
   private NeustarRNDiscountCode                       cloudNameDiscountCode                = NeustarRNDiscountCode.OnePersonOneName;

   /* CHOOSE A RESPECT NETWORK MEMBERSHIP DISCOUNT CODE */
   private static RespectNetworkRNDiscountCode respectNetworkMembershipDiscountCode = RespectNetworkRNDiscountCode.IIW17;

   public void register(String env, String cspName,
         String cspPassword, String cloudNameString, String cloudNumberString, String secretToken,String verifiedPhone, String verifiedEmail )
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
               XDI3Segment.create("+" + cspName), null);
         
         cspCloudNumber = discResult.getCloudNumber();
         System.out.println("CSP Cloud Number : " +cspCloudNumber.toString());

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
            XDI3Segment
                  .create("([+]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa/#registrar)$do"),
            null, discoveryClient);

      System.out.println("CSP Information:\n" +cspInformation.toString());
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
      if (cloudNumberString == null || cloudNumberString.isEmpty())
         cloudNumberString = "[=]!:uuid:" + UUID.randomUUID().toString();

      cloudNumber = CloudNumber.create(cloudNumberString);
      if (cloudName == null)
      {

         System.err.println("Invalid Cloud Number.");
         System.exit(0);
      }
      try
      {

         CSP csp = new BasicCSP(cspInformation);

         // Step 1: Register Cloud with Cloud Number and Shared Secret

         csp.registerCloudInCSP(cloudNumber, secretToken);

         // step 2: Set Cloud Services in Cloud

         Map<XDI3Segment, String> services = new HashMap<XDI3Segment, String>();

         try
         {
            services.put(
                  XDI3Segment.create("<$https><$connect><$xdi>"),
                  "https://respectconnect.neustar.biz/"
                        + URLEncoder.encode(cloudNumber.toString(), "UTF-8")
                        + "/connect/request");
         } catch (UnsupportedEncodingException e)
         {
            throw new CSPRegistrationException(e);
         }

         csp.setCloudServicesInCloud(cloudNumber, secretToken, services);

         // step 3: Check if the Cloud Name is available

         boolean cloudNameAvailable = csp
               .checkCloudNameAvailableInRN(cloudName);

         if (! cloudNameAvailable)
            throw new RuntimeException("Cloud Name " + cloudName
                  + " is not available.");

         // Step 4: Check if the phone number and e-mail address are available

         CloudNumber[] existingCloudNumbers = csp
               .checkPhoneAndEmailAvailableInRN(verifiedPhone, verifiedEmail);

         if (existingCloudNumbers[0] != null)
            throw new RuntimeException(
                  "This verified phone number is already registered with Cloud Number "
                        + existingCloudNumbers[0] + ".");
         if (existingCloudNumbers[1] != null)
            throw new RuntimeException(
                  "This verified e-mail address is already registered with Cloud Number "
                        + existingCloudNumbers[1] + ".");

         // step 5: Register Cloud Name

         csp.registerCloudNameInRN(cloudName, cloudNumber, verifiedPhone,
               verifiedEmail, cloudNameDiscountCode);
         csp.registerCloudNameInCSP(cloudName, cloudNumber);
         csp.registerCloudNameInCloud(cloudName, cloudNumber, secretToken);

         // step 6: Set phone number and e-mail address

         csp.setPhoneAndEmailInCloud(cloudNumber, secretToken, verifiedPhone,
               verifiedEmail);

         // step 7: Set RN/RF membership

         csp.setRespectNetworkMembershipInRN(cloudNumber, new Date(), null);
         csp.setRespectFirstMembershipInRN(cloudNumber);

         // Step 8: Change Secret Token

         csp.setCloudSecretTokenInCSP(cloudNumber, secretToken);

         // done

         System.out.println("Done registering Cloud Name " + cloudName
               + " with Cloud Number " + cloudNumber + " and "
               + services.size() + " services.");
      } catch (CSPRegistrationException ex1)
      {
         System.out
               .println("Failed to register cloudname with CSP. CloudName : "
                     + cloudName.toString() + " , CloudNumber : "
                     + cloudNumber.toString());

         System.out.println("CSPRegistrationException  " + ex1.getMessage());
      } catch (Xdi2ClientException ex2)
      {
         System.out.println("Failed to register cloudname. CloudName : "
               + cloudName.toString() + " , CloudNumber : "
               + cloudNumber.toString());

         System.out.println("Xdi2ClientException  " + ex2.getMessage());
      } 
   }

   public static void main(String[] args) throws Exception
   {
      TLSv1Support.supportTLSv1();

      System.out.print("Environment(ote/prod/prod-au :");
      String str = new BufferedReader(new InputStreamReader(System.in))
            .readLine();
      while (str != null && (str.equalsIgnoreCase("ote") || (str.equalsIgnoreCase("prod")) || (str.equalsIgnoreCase("prod-au"))))
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
         String cloudNameString = new BufferedReader(new InputStreamReader(System.in))
               .readLine();
         System.out.print("CloudNumber :");
         String cloudNumberString = new BufferedReader(new InputStreamReader(
               System.in)).readLine();
         System.out.print("Cloud password :");
         String cloudPass = new BufferedReader(new InputStreamReader(
               System.in)).readLine();
         System.out.print("Verified Phone :");
         String verifiedPhone = new BufferedReader(new InputStreamReader(
               System.in)).readLine();
         System.out.print("Verified Email :");
         String verifiedEmail = new BufferedReader(new InputStreamReader(
               System.in)).readLine();
         System.out.println("Creating cloudname with this information :");
         System.out.println("Env : " + env);
         System.out.println("CSP Name : " + cspName);
         System.out.println("Cloud Name: " + cloudNameString);
         System.out.println("Cloud Number:" + cloudNumberString);
         System.out.println("Email :" + verifiedEmail);
         System.out.println("Phone :" + verifiedPhone);
         System.out.print("Continue ? Y/N :");
         String toContinue = new BufferedReader(new InputStreamReader(
               System.in)).readLine();
         if(!toContinue.equalsIgnoreCase("y"))
         {
            break;
         }
         RegisterCloudName regMachine = new RegisterCloudName();
         regMachine.register(env, cspName, cspPassword, cloudNameString, cloudNumberString,cloudPass,verifiedPhone,verifiedEmail);
         System.out.print("Create another ? Y/N :");
         toContinue = new BufferedReader(new InputStreamReader(
               System.in)).readLine();
         if(!toContinue.equalsIgnoreCase("y"))
         {
            break;
         }
      }

   }
}
