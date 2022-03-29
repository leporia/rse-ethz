package ch.ethz.rse.integration;

import java.io.IOException;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import ch.ethz.rse.testing.VerificationTestCase;
import ch.ethz.rse.testing.VerificationTestCaseCollector;

/**
 * Collects all available tasks
 * 
 */
public class VerificationTestCaseCollectorTest {

	/**
	 * check that at least one task was found
	 */
	@Test
	public void checkTasksExist() throws IOException {
		List<VerificationTestCase> tasks = VerificationTestCaseCollector.getTests();
		MatcherAssert.assertThat(tasks.size(), Matchers.greaterThan(0));
	}

}
