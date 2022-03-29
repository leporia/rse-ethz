package ch.ethz.rse.testing;

import java.io.File;
import java.io.FileNotFoundException;

import ch.ethz.rse.VerificationProperty;
import ch.ethz.rse.VerificationResult;
import ch.ethz.rse.VerificationTask;
import ch.ethz.rse.utils.Configuration;
import ch.ethz.rse.verify.ClassToVerify;

/**
 * Convenience wrapper that describes a test case
 * 
 */
public class VerificationTestCase implements Comparable<VerificationTestCase> {

	private final String packageName;
	private final ClassToVerify tc;
	public final VerificationProperty verificationProperty;
	public final VerificationResult expected;
	private boolean disabled = false;

	public VerificationTestCase(String packageName, VerificationProperty verificationTask, boolean expectedIsSafe) {
		try {
			this.packageName = packageName;
			String basedir = Configuration.props.getBasedir();
			File classPath = new File(basedir + "/target/test-classes");
			this.tc = new ClassToVerify(classPath, packageName);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Did you compile your tests, e.g., using `mvn test-compile`?", e);
		}
		this.verificationProperty = verificationTask;
		this.expected = new VerificationResult(expectedIsSafe);
	}

	public ClassToVerify getTestClass() {
		return this.tc;
	}

	public VerificationTask getVerificationTask() {
		return new VerificationTask(this.tc.getPackageName(), this.verificationProperty);
	}

	public boolean isDisabled() {
		return this.disabled;
	}

	public void disable() {
		this.disabled = true;
	}

	@Override
	public String toString() {
		return this.tc.getName() + ":" + this.verificationProperty.toString();
	}

	/***
	 * 
	 * @return a CSV representation of this test case
	 */
	public String toSCV() {
		return this.tc.getPackageName() + "," + this.verificationProperty.toString() + "," + this.expected.toString();
	}

	@Override
	public int compareTo(VerificationTestCase w) {
		// comparison function to allow sorting
		int cmp = this.packageName.compareTo(w.packageName);
		if (cmp == 0) {
			return this.verificationProperty.compareTo(w.verificationProperty);
		} else {
			return cmp;
		}
	}

	/**
	 * disables all test cases in iterable
	 * @param iterable
	 */
	public static void disableAll(Iterable<VerificationTestCase> iterable) {
		for (VerificationTestCase t : iterable) {
			t.disable();
		}
	}

}
