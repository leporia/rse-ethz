package ch.ethz.rse.verify;

import ch.ethz.rse.VerificationProperty;
import ch.ethz.rse.numerical.NumericalAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.SootMethod;

import java.util.HashMap;
import java.util.Map;


public abstract class AVerifier {

	private static final Logger logger = LoggerFactory.getLogger(AVerifier.class);
	
	/**
	 * result of running numerical analysis, per method
	 */
	protected final Map<SootMethod, NumericalAnalysis> numericalAnalysis = new HashMap<SootMethod, NumericalAnalysis>();

	/**
	 * 
	 * @param property
	 * @return true if <code>property</code> is SAFE, false if it may be UNSAFE
	 */
	public boolean check(VerificationProperty property) {
		long startTime = System.nanoTime();

		this.runNumericalAnalysis(property);

		boolean ret;
		switch (property) {
		case START_END_ORDER:
			ret = this.checkStartEndOrder();
			break;
		case AFTER_START:
			ret = this.checkAfterStart();
			break;
		case BEFORE_END:
			ret = this.checkBeforeEnd();
			break;
		default:
			throw new UnsupportedOperationException(property.toString());
		}

		long endTime = System.nanoTime();
		long durationMilliseconds = (endTime - startTime) / 1000000;
		logger.debug("Runtime: Checked property {} in {}ms", property, durationMilliseconds);

		return ret;
	}

	/**
	 * 
	 * @return true if START_END_ORDER is SAFE, false if it may be UNSAFE
	 */
	protected abstract boolean checkStartEndOrder();

	/**
	 * 
	 * @return true if AFTER_START is SAFE, false if it may be UNSAFE
	 */
	protected abstract boolean checkAfterStart();

	/**
	 * 
	 * @return true if BEFORE_END is SAFE, false if it may be UNSAFE
	 */
	protected abstract boolean checkBeforeEnd();

	/**
	 * Populate {@link #numericalAnalysis}
	 * 
	 * @param property the property about to be verified
	 */
	protected abstract void runNumericalAnalysis(VerificationProperty property);
}
