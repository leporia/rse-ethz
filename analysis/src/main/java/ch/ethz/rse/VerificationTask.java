package ch.ethz.rse;

import java.io.File;
import java.io.FileNotFoundException;

import ch.ethz.rse.utils.Configuration;
import ch.ethz.rse.verify.ClassToVerify;

/**
 * Convenience wrapper that describes a specific verification task
 * 
 */
public class VerificationTask implements Comparable<VerificationTask> {

	private final String packageName;
	private final ClassToVerify tc;
	public final VerificationProperty property;

	public VerificationTask(String packageName, VerificationProperty property) {
		try {
			this.packageName = packageName;
			String basedir = Configuration.props.getBasedir();
			File classPath = new File(basedir + "/target/test-classes");
			this.tc = new ClassToVerify(classPath, packageName);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Did you compile your tests, e.g., using `mvn test-compile`?", e);
		}
		this.property = property;
	}

	public ClassToVerify getTestClass() {
		return this.tc;
	}

	@Override
	public String toString() {
		return this.tc.getName() + ":" + this.property.toString();
	}

	public String toLongString() {
		return this.tc.getPackageName() + ":" + this.property.toString();
	}

	@Override
	public int compareTo(VerificationTask w) {
		// comparison function to allow sorting
		int cmp = this.packageName.compareTo(w.packageName);
		if (cmp == 0) {
			return this.property.compareTo(w.property);
		} else {
			return cmp;
		}
	}

}
