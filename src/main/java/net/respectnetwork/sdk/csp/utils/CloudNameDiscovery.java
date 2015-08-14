package net.respectnetwork.sdk.csp.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Set;

import net.respectnetwork.sdk.csp.ssl.TLSv1Support;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.syntax.CloudName;
import xdi2.core.syntax.XDIAddress;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;

public class CloudNameDiscovery
{

   /* CHOOSE THE CLOUD NAME HERE */
   private static CloudName          cloudName;

   private static XDIDiscoveryClient discovery;

   /* CHOOSE THE endpoints */
   private static ArrayList<String>  endpoints  = new ArrayList<String>();

   private static XDIAddress[]      epSegments = null;

   static
   {
      TLSv1Support.supportTLSv1();
      try
      {
         System.out.print("Enter environment: PROD or OTE :");
         String env = new BufferedReader(new InputStreamReader(System.in))
               .readLine();
         if (env.equalsIgnoreCase("OTE"))
         {
            discovery = XDIDiscoveryClient.NEUSTAR_OTE_DISCOVERY_CLIENT;
         } else if (env.equalsIgnoreCase("PROD"))
         {
            discovery = XDIDiscoveryClient.NEUSTAR_PROD_DISCOVERY_CLIENT;
         } else
         {
            System.out.print("Environment has to be one of PROD or OTE ");
            System.exit(0);
         }

         System.out.print("Enter Cloud Name: ");
         cloudName = CloudName.create(new BufferedReader(new InputStreamReader(
               System.in)).readLine());

         if (cloudName == null)
         {
            System.err.println("Invalid Cloud Name.");
            System.exit(0);
         }
         System.out
               .print("Enter EndpointURI address. Type DONE when finished. :");
         BufferedReader br = new BufferedReader(
               new InputStreamReader(System.in));
         String endpointAddress = new String();
         while (!(endpointAddress = br.readLine()).equalsIgnoreCase("DONE"))
         {
            System.out.print("Enter EndpointURI address: ");
            endpoints.add(endpointAddress);
         }

         epSegments = new XDIAddress[endpoints.size()];
         for (int i = 0; i < endpoints.size(); i++)
         {
            epSegments[i] = XDIAddress.create(endpoints.get(i));
         }

      } catch (IOException ex)
      {

         throw new RuntimeException(ex.getMessage(), ex);
      }
   }

   public static void main(String[] args) throws Exception
   {

      // Resolve Cloud Numbers from Name

      try
      {
         XDIDiscoveryResult discResult = discovery.discover(
                 XDIAddress.create(cloudName.toString()), epSegments);
         System.out.println("CloudNumber : " + discResult.getCloudNumber());
         System.out.println("xdi endpoint : " + discResult.getXdiEndpointUri());
         if (discResult.getEndpointUris() != null)
         {
            Set<XDIAddress> keyset = discResult.getEndpointUris().keySet();
            XDIAddress[] keys = keyset.toArray(new XDIAddress[0]);
            for (int i = 0; i < keys.length; i++)
            {
               System.out.println("Endpoint Address :" + keys[i] + ", URI : "
                     + discResult.getEndpointUris().get(keys[i]));
            }
         }

      } catch (Xdi2ClientException e)
      {
         System.out.println("Xdi2ClientException: " + e.getMessage());
         e.printStackTrace();
      }

   }
}
