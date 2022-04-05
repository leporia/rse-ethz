package ch.ethz.rse.testing;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import ch.ethz.rse.VerificationProperty;
import ch.ethz.rse.utils.Configuration;
import ch.ethz.rse.utils.Constants;

/**
 * Collects all available tasks
 */
public class VerificationTestCaseCollector {

	private static final Logger logger = LoggerFactory.getLogger(VerificationTestCaseCollector.class);

	/**
	 * Test files are ignore if they contain this string
	 */
	private static final String DISABLED = "DISABLED";

	/**
	 * If a test file contains this string, all other test files are ignored
	 */
	private static final String DISABLE_OTHERS = "DISABLE_OTHERS";

	/**
	 * Properties in this set are not checked
	 */
	private static final Set<VerificationProperty> IGNORED = Sets.newHashSet();

	/**
	 * Search for tests in this package
	 */
	private static final String testPackage = "ch.ethz.rse.integration.tests";

	/**
	 * 
	 * @return a list of all tasks obtained from {@link #testPackage}
	 */
	public static List<VerificationTestCase> getTests() throws IOException {
		// get directory of tests
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

			if (content.contains(DISABLE_OTHERS)) {
				VerificationTestCase.disableAll(tasks);
			}

			for (VerificationProperty p : VerificationProperty.values()) {
				VerificationTestCase t = null;
				if (content.contains(p + " " + Constants.safe)) {
					t = new VerificationTestCase(packageName, p, true);
				} else if (content.contains(p + " " + Constants.unsafe)) {
					t = new VerificationTestCase(packageName, p, false);
				}
				if (t != null) {
					if (isDisabled(t, f, content) || disableOthers) {
						t.disable();
					}
					tasks.add(t);
				}
			}

			if (content.contains(DISABLE_OTHERS)) {
				disableOthers = true;
			}
			
		}

		Collections.sort(tasks);

		assert tasks.size() > 0;

		VerificationTestCaseCollector.printStatistics(examplesPath, tasks);
		return tasks;
	}

	private static void printStatistics(String examplesPath, List<VerificationTestCase> tasks) {
		logger.info("Collected {} tests from {}", tasks.size(), examplesPath);

		int[][] counts = new int[VerificationProperty.values().length][2];
		for (VerificationTestCase t : tasks) {
			int safe = t.expected.isSafe ? 0 : 1;
			counts[t.verificationProperty.ordinal()][safe]++;
		}
		for (VerificationProperty p : VerificationProperty.values()) {
			String s = String.format("%s: %s SAFE + %s UNSAFE",
				Strings.padEnd(p.toString(), 14, ' '),
				Strings.padStart(Integer.toString(counts[p.ordinal()][0]), 2, ' '),
				Strings.padStart(Integer.toString(counts[p.ordinal()][1]), 2, ' ')
			);
			logger.info(s);
		}
	}

	private static boolean isDisabled(VerificationTestCase t, File f, String content) {
		if (content.contains(DISABLED)) {
			return true;
		} else if (IGNORED.contains(t.verificationProperty)) {
			return true;
		}
		return false;
	}

	public static void main(String[] args) throws IOException {
		String path = Configuration.props.getBasedir() + File.separator + "target" + File.separator + "tests.csv";
		File testCasesFile = new File(path);

		FileUtils.writeStringToFile(testCasesFile, "packageName,property,expected\n", Charset.defaultCharset(), false);


		for (VerificationTestCase t : VerificationTestCaseCollector.getTests()) {
			String line = t.toSCV();
			FileUtils.writeStringToFile(testCasesFile, line + "\n", Charset.defaultCharset(), true);
		}
	}


}
