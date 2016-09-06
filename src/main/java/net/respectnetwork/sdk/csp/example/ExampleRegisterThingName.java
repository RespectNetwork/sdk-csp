package net.respectnetwork.sdk.csp.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

import net.respectnetwork.sdk.csp.BasicCSP;
import net.respectnetwork.sdk.csp.BasicCSPInformation;
import net.respectnetwork.sdk.csp.CSP;
import xdi2.core.constants.XDIConstants;
import xdi2.core.syntax.CloudName;
import xdi2.core.syntax.CloudNumber;

public class ExampleRegisterThingName {

    /* THING CLOUD NUMBER */
    private static CloudNumber thingCloudNumber;

    /* THING CLOUD NAME */
    private static CloudName thingCloudName;

    /* THING Cloud Contact Cloud Number */
    private static CloudNumber contactCloudNumber;

    static {

        try {

            System.out.print("Enter Thing Cloud Name (leave blank for a random one): ");

            String cloudNameString = new BufferedReader(new InputStreamReader(System.in)).readLine();
            if (cloudNameString.trim().isEmpty())
                cloudNameString = "*dev.test." + UUID.randomUUID().toString();
            thingCloudName = CloudName.create(cloudNameString);

            if (thingCloudName == null) {

                System.err.println("Invalid Cloud Name.");
                System.exit(0);
            }

            System.out.print("Enter Thing Cloud Number (leave blank for a random one): ");
            String cloudNumberString = new BufferedReader(new InputStreamReader(System.in)).readLine();
            if (cloudNumberString.trim().isEmpty()) {
                thingCloudNumber = CloudNumber.createRandom(thingCloudName.getCs());
            } else {
                thingCloudNumber = CloudNumber.create(cloudNumberString);
            }

            if (thingCloudNumber == null) {

                System.err.println("Invalid Business Cloud Number.");
                System.exit(0);
            }

            System.out.print("Enter Thing Contact  Cloud Number (leave blank for a random one): ");
            String contactCloudNumberString = new BufferedReader(new InputStreamReader(System.in)).readLine();
            if (contactCloudNumberString.trim().isEmpty())
                contactCloudNumber = CloudNumber.createRandom(XDIConstants.CS_AUTHORITY_LEGAL);

            if (contactCloudNumber == null) {

                System.err.println("Invalid Contact Cloud Number.");
                System.exit(0);
            }

        } catch (IOException ex) {

            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static void main(String[] args) throws Exception {

        // Step 0: Set up CSP

        BasicCSPInformation cspInformation = new CSPInformationTestCSPSandbox();

        cspInformation.retrieveCspSignaturePrivateKey();
        cspInformation.setRnCspSecretToken(null);

        CSP csp = new BasicCSP(cspInformation);

        // Step 1: Register Business Cloud Name and Create Member Graph Entry.

        // @TODO Question: Is this sufficient? There are Non RN CloudNumbers!!
        // Call Check Availability Instead for more Valid Test!!!
        boolean available = true;// csp.checkCloudNameAvailableInRN(thingCloudName);

        if (!available) {
            System.out.println(thingCloudName + " is not available.");
            System.exit(0);
        }

        csp.registerThingNameInRN(thingCloudName, thingCloudNumber, contactCloudNumber);

        // Step 2: Register Additional Cloud Name in CSP Graph

        csp.registerThingNameInCSP(thingCloudName, thingCloudNumber, contactCloudNumber);

        // Step 3: Register Additional Cloud Name in User Graph

        csp.registerThingNameInCloud(thingCloudName, thingCloudNumber, contactCloudNumber);

        System.out.println("Done registering Thing Cloud Name " + thingCloudName + " with Cloud Number "
                + thingCloudNumber);
    }
}
