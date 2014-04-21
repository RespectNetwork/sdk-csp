package net.respectnetwork.sdk.csp.performance;

import java.security.GeneralSecurityException;
import java.util.Random;

import net.respectnetwork.sdk.csp.BasicCSPInformation;
import xdi2.client.exceptions.Xdi2ClientException;

public class StressTest implements Runnable {

	/** Number Of Threads in the test */
	private static int threads;

	/** Number of iterations to run per test */
	private static int iterations;

	/** Test to Run */
	private static String testClass;

	/** Env to Run In */
	private static String env;

	/**
	 * Simple Multi Threaded Test Harness
	 */
	public static void main(String[] args) {

		String USAGE = "USAGE: StressTest  [number of  threads] [number of iterations] [test]  [environment] ";
		if (args.length != 4) {
			System.out.println(USAGE);
			return;
		}

		threads = new Integer(args[0]).intValue();
		iterations = new Integer(args[1]).intValue();
		testClass = args[2];
		env = args[3];

		Thread[] myThreads = new Thread[threads];
		for (int i = 0; i < threads; i++) {
			myThreads[i] = (new Thread(new StressTest()));
		}

		for (int i = 0; i < threads; i++) {
			myThreads[i].start();
		}
	}

	/**
	 * Thread Run Method
	 */
	public void run() {

		float successCount = 0;
		String name = Thread.currentThread().getName();
		System.out.println("Starting " + name);
		long begin = System.currentTimeMillis();

		for (int i = 0; i < iterations; i++) {
			if (performTest(i + 1, name)) {
				successCount++;
			}
		}

		long end = System.currentTimeMillis();
		long average = (end - begin) / iterations;
		long fullduration = (end - begin);
		float successrate = (successCount / iterations) * 100;

		System.out.println("#" + name + " Finished: Elapsed Time = "
				+ fullduration + "ms" + " AverageTime = " + average
				+ "ms SuccessRate = " + successrate + "%");
	}

	/**
	 * Perform the Test Details.
	 * 
	 * @param iteration
	 * @param name
	 * @return
	 */
	public boolean performTest(int iteration, String name) {

		boolean success = false;
		long begin = 0;
		long end = 0;
		long fullduration = 0;

		try {
			begin = System.currentTimeMillis();

			// Pick the Environment you want to run the test in.
			BasicCSPInformation cspInformation = configureEnvironment(
					TestEnv.valueOf(env), false);

			// Pick the tests you want to run.
			Class c = Class.forName(testClass);

			Tester tester = (Tester) c.newInstance();
			tester.setCspInformation(cspInformation);
			tester.init();

			try {
				Thread.sleep(randomDelay(0, 50));
				tester.execute();
				success = true;
			} catch (TestException e) {
				System.out.println("Stress Test Exception: " + e.getMessage());
				success = false;
			}

			end = System.currentTimeMillis();
			fullduration = (end - begin);

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			success = false;

		}

		System.out.println("#" + name + " Task #" + iteration + " Duration = "
				+ fullduration + "ms" + " Success = " + success);

		return success;
	}

	public BasicCSPInformation configureEnvironment(TestEnv env, boolean signing)
			throws TestException {

		BasicCSPInformation cspInfo;

		switch (env) {

		case PERFTEST:
			cspInfo = new CSPInformationPERFTestCsp();
			break;

		case STAGETEST:
			cspInfo = new CSPInformationSTAGETestCsp();
			break;

		default:
			cspInfo = new CSPInformationPERFTestCsp();
			break;

		}

		// Configure for Signed Messaging.
		if (signing) {
			try {
				cspInfo.setRnCspSecretToken(null);
				cspInfo.retrieveCspSignaturePrivateKey();
			} catch (GeneralSecurityException e) {
				throw new TestException();
			} catch (Xdi2ClientException e) {
				throw new TestException();
			}
		}

		return cspInfo;
	}

	/**
	 * Random Number Generator for use in creating ramdom delay.
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	public int randomDelay(int min, int max) {

		Random rand = new Random();
		int randomNum = rand.nextInt((max - min) + 1) + min;
		return randomNum;
	}

	/**
	 * Enum to describe Test Config
	 * 
	 */
	enum TestEnv {

		DEVTEST("DEV", "testcsp"), PERFTEST("PERF", "testcsp"), LOCALTEST(
				"LOCAL", "testcsp"), STAGETEST("STAGE", "testcsp");

		private final String env;
		private final String csp;

		TestEnv(String env, String csp) {
			this.env = env;
			this.csp = csp;
		}

		public String getEnv() {
			return env;
		}

		public String getCsp() {
			return csp;
		}

	}

}