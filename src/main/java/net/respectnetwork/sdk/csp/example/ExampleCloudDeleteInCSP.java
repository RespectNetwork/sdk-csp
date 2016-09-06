package net.respectnetwork.sdk.csp.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import net.respectnetwork.sdk.csp.BasicCSP;
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.CSPInformation;
import xdi2.core.syntax.CloudName;
import xdi2.core.syntax.CloudNumber;

public class ExampleCloudDeleteInCSP {

    /* CHOOSE THE INDIVIDUAL's CLOUD NUMBER HERE */
    private static CloudNumber cloudNumber;

    static {
        try {
            System.out.print("Enter Cloud Number: ");
            cloudNumber = CloudNumber.create(new BufferedReader(new InputStreamReader(System.in)).readLine());

            if (cloudNumber == null) {
                System.err.println("Invalid Cloud Number.");
                System.exit(0);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static void main(String[] args) throws Exception {

        // Step 0: Set up CSP

        CSPInformation cspInformation = new CSPInformationTestCSPSandbox();
        CSP csp = new BasicCSP(cspInformation);
        CloudName[] cloudNames = csp.checkCloudNamesInCSP(cloudNumber);
        System.out.println("Before Delete CloudNames size:" + cloudNames.length);
        csp.deleteCloudInCSP(cloudNumber, null);
        System.out.println("Done deleting Cloud  Number:" + cloudNumber);
        cloudNames = csp.checkCloudNamesInCSP(cloudNumber);
        System.out.println("After Delete CloudNames size=======" + cloudNames.length);
    }
}
