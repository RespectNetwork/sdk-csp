package net.respectnetwork.sdk.csp.example;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.respectnetwork.sdk.csp.BasicCSP;
import net.respectnetwork.sdk.csp.BasicCSPInformation;
import net.respectnetwork.sdk.csp.CSP;

import org.apache.commons.lang.RandomStringUtils;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.syntax.CloudName;
import xdi2.core.syntax.CloudNumber;

public class ExampleRegisterConcurrentClouds {

    /* CHOOSE THE INDIVIDUAL's SECRET TOKEN HERE */
    private static String secretToken = "Test@123";

    /* CHOOSE THE INDIVIDUAL's VERIFIED PHONE NUMBER HERE */
    private static String verifiedPhone = "myphone-" + UUID.randomUUID().toString();

    /* CHOOSE THE INDIVIDUAL's VERIFIED EMAIL HERE */
    private static String verifiedEmail = "kapil.vats@impetus.co.in";

    // Number of thread span at single time. Increase count to replicate the
    // concurrency issue.
    private static final int MYTHREADS = 2;

    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(MYTHREADS);

        BasicCSPInformation cspInformation = new CSPInformationTestCSPQA();
        cspInformation.retrieveCspSignaturePrivateKey();
        cspInformation.setRnCspSecretToken(null);
        CSP csp = new BasicCSP(cspInformation);

        for (int i = 0; i < MYTHREADS; i++) {
            Runnable worker = new CreateCloudThread(createCloudInfo(), csp);
            executor.execute(worker);
        }
        executor.shutdown();
        // Wait until all threads are finish
        while (!executor.isTerminated()) {

        }
        System.out.println("\nFinished all threads");

    }

    private static CloudInfo createCloudInfo() {
        CloudInfo cloudInfo = new CloudInfo();
        cloudInfo.setCloudName(CloudName.create("="
                + RandomStringUtils.random(4, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz")));
        cloudInfo.setCloudNumber(CloudNumber.createRandom(cloudInfo.getCloudName().getCs()));
        cloudInfo.setPhone(verifiedPhone);
        cloudInfo.setEmail(verifiedEmail);
        cloudInfo.setPassword(secretToken);
        return cloudInfo;
    }

    static class CloudInfo {
        private CloudNumber cloudNumber;
        private CloudName cloudName;
        private String phone;
        private String email;
        private String password;

        /**
         * @return the cloudNumber
         */
        public CloudNumber getCloudNumber() {
            return cloudNumber;
        }

        /**
         * @param cloudNumber
         *            the cloudNumber to set
         */
        public void setCloudNumber(CloudNumber cloudNumber) {
            this.cloudNumber = cloudNumber;
        }

        /**
         * @return the cloudName
         */
        public CloudName getCloudName() {
            return cloudName;
        }

        /**
         * @param cloudName
         *            the cloudName to set
         */
        public void setCloudName(CloudName cloudName) {
            this.cloudName = cloudName;
        }

        /**
         * @return the phone
         */
        public String getPhone() {
            return phone;
        }

        /**
         * @param phone
         *            the phone to set
         */
        public void setPhone(String phone) {
            this.phone = phone;
        }

        /**
         * @return the email
         */
        public String getEmail() {
            return email;
        }

        /**
         * @param email
         *            the email to set
         */
        public void setEmail(String email) {
            this.email = email;
        }

        /**
         * @return the password
         */
        public String getPassword() {
            return password;
        }

        /**
         * @param password
         *            the password to set
         */
        public void setPassword(String password) {
            this.password = password;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("CloudInfo [cloudNumber=");
            builder.append(cloudNumber);
            builder.append(", cloudName=");
            builder.append(cloudName);
            builder.append("]");
            return builder.toString();
        }

    }

    public static class CreateCloudThread implements Runnable {
        private final CloudInfo cloudInfo;
        private final CSP csp;

        CreateCloudThread(CloudInfo cloudInfo, CSP csp) {
            this.cloudInfo = cloudInfo;
            this.csp = csp;
        }

        @Override
        public void run() {
            System.out.println("Starting registering Cloud with details :" + cloudInfo.toString());
            try {
                csp.registerCloudInCSP(cloudInfo.getCloudNumber(), secretToken);

                csp.registerCloudNameInRN(cloudInfo.getCloudName(), cloudInfo.getCloudNumber(), verifiedPhone,
                        verifiedEmail, null);

                csp.registerCloudNameInCSP(cloudInfo.getCloudName(), cloudInfo.getCloudNumber());

                csp.registerCloudNameInCloud(cloudInfo.getCloudName(), cloudInfo.getCloudNumber(), secretToken);

                csp.setPhoneAndEmailInCloud(cloudInfo.getCloudNumber(), secretToken, verifiedPhone, verifiedEmail);

            } catch (Xdi2ClientException e) {
                System.out.println("Error registering Cloud with details :" + cloudInfo.toString());
                e.printStackTrace();
            }

            System.out.println("Done registering Cloud with details :" + cloudInfo.toString());
        }
    }
}
