package ch.ethz.rse.integration;

import ch.ethz.rse.VerificationProperty;
import ch.ethz.rse.testing.VerificationTestCase;
import ch.ethz.rse.testing.VerificationTestCaseCollector;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * Test the code on all provided examples
 * 
 */
public class AllExamplesIT_Error {

	/**
	 * 
	 * @return all available tasks
	 */
	public static List<VerificationTestCase> getTests() throws IOException {
		String testPackage = "ch.ethz.rse.integration.invalid_input_tests";
		String examplesPath = System.getProperty("user.dir") + "/src/test/java/" + testPackage.replace(".", File.separator);
		File examplesDir = new File(examplesPath);

		// collect tasks
		List<VerificationTestCase> tasks = new LinkedList<VerificationTestCase>();
		boolean disableOthers = false;

		for (File f : examplesDir.listFiles()) {
			// skip directories
			if (f.isDirectory()) {
				continue;
			}

			String content = Files.asCharSource(f, Charsets.UTF_8).read();
			String className = FilenameUtils.removeExtension(f.getName());
			String packageName = testPackage + "." + className;

			VerificationTestCase t = new VerificationTestCase(packageName, VerificationProperty.START_END_ORDER, false);
			tasks.add(t);
			
		}

		Collections.sort(tasks);

		assert tasks.size() > 0;

		return tasks;
	}

	@ParameterizedTest(name = "{index}: {0}")
	@MethodSource("getTests")
	void testExampleClass(VerificationTestCase example) {
		ErrorTest.testOnExample(example);
	}

}
