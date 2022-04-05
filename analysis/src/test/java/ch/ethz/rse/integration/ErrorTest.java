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
public class ErrorTest {

	/**
	 * Modify the configuration below to run a single example
	 */
	@Test
	void specificTest() {
		//String packageName = "ch.ethz.rse.integration.invalid_input_tests.Basic_Test_Invalid";
		String packageName = "ch.ethz.rse.integration.invalid_input_tests.Basic_Test_Global";
		VerificationTestCase t = new VerificationTestCase(packageName, VerificationProperty.START_END_ORDER, true);
		ErrorTest.testOnExample(t);
	}


	private static final Logger logger = LoggerFactory.getLogger(ErrorTest.class);

	public static void testOnExample(VerificationTestCase example) {

		Assumptions.assumeFalse(example.isDisabled());
		AssertionError error = new AssertionError("Expected exception");

		try {
			Runner.verify(example.getVerificationTask());
			
			throw error;
		} catch (Throwable e) {
			// all well
			if (e.equals(error)) {
				logger.error("Exception for example {}: {}", example, e);
				throw e;
			}
		}
	}
}
