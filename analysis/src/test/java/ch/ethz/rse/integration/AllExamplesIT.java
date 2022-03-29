package ch.ethz.rse.integration;

import ch.ethz.rse.testing.VerificationTestCase;
import ch.ethz.rse.testing.VerificationTestCaseCollector;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;

/**
 * Test the code on all provided examples
 * 
 */
public class AllExamplesIT {

	/**
	 * 
	 * @return all available tasks
	 */
	public static List<VerificationTestCase> getTests() throws IOException {
		return VerificationTestCaseCollector.getTests();
	}

	@ParameterizedTest(name = "{index}: {0}")
	@MethodSource("getTests")
	void testExampleClass(VerificationTestCase example) {
		SpecificExampleIT.testOnExample(example);
	}

}
