package ch.ethz.rse.main;

import ch.ethz.rse.VerificationTask;
import ch.ethz.rse.VerificationResult;
import ch.ethz.rse.verify.AVerifier;
import ch.ethz.rse.verify.ClassToVerify;
import ch.ethz.rse.verify.Verifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.SootClass;
import soot.SootHelper;

/**
 * Convenience wrapper for verifying a given {@link VerificationTask}
 */
public class Runner {
	
	private static final Logger logger = LoggerFactory.getLogger(Runner.class);
	
	public static VerificationResult verify(VerificationTask t) {
		long startTime = System.nanoTime();

		VerificationResult ret = Runner.verifyInternal(t);

		long endTime = System.nanoTime();
		long durationMilliseconds = (endTime - startTime) / 1000000;
		logger.debug("Runtime: Verified {} in {}ms", t, durationMilliseconds);

		return ret;
	}

	private static VerificationResult verifyInternal(VerificationTask t) {
		logger.debug("Verifying {}", t.toString());

		ClassToVerify tc = t.getTestClass();

		// load analyzed class
		SootClass c = SootHelper.loadClassAndAnalyze(tc);

		VerificationResult ret;

		AVerifier v = new Verifier(c);
		boolean isSafe = v.check(t.property);
		ret = new VerificationResult(isSafe);

		return ret;
	}
}