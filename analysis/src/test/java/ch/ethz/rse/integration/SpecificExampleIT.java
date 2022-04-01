package ch.ethz.rse.integration;

import ch.ethz.rse.VerificationProperty;
import ch.ethz.rse.VerificationResult;
import ch.ethz.rse.main.Runner;
import ch.ethz.rse.testing.VerificationTestCase;
import com.google.common.base.Strings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows to easily run an integration test for a single example
*/
public class SpecificExampleIT {

	/**
	 * Modify the configuration below to run a single example
	 */
	@Test
	void specificTest() {
		String packageName = "ch.ethz.rse.integration.tests.Basic_Variable_Test_Unsafe";
		VerificationProperty verificationTask = VerificationProperty.BEFORE_END;
		boolean expectedIsSafe = false;
		VerificationTestCase t = new VerificationTestCase(packageName, verificationTask, expectedIsSafe);
		SpecificExampleIT.testOnExample(t);
	}


	private static final Logger logger = LoggerFactory.getLogger(SpecificExampleIT.class);

	public static void testOnExample(VerificationTestCase example) {

		Assumptions.assumeFalse(example.isDisabled());

		try {
			VerificationResult actual = Runner.verify(example.getVerificationTask());
			
			// check result
			SpecificExampleIT.compare(example.toString(), example.expected, actual);
			Assertions.assertEquals(example.expected, actual);
		} catch (Throwable e) {
			logger.error("Exception for example {}: {}", example, e);
			throw e;
		}
	}

	// LOGGING RESULTS

	private static void compare(String label, VerificationResult expected, VerificationResult actual) {
		String cmp = actual.compare(expected);

		String s = String.format("%s (expected:%s,got:%s)", label, expected.toString(), actual.toString());
		s = Strings.padEnd(s, 75, ' ');
		String summary = String.format("%s: %s", s, cmp);
		logger.info(summary);
	}

}
