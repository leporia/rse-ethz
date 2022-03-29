package ch.ethz.rse;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import ch.ethz.rse.utils.Constants;

/**
 * Convenience wrapper storing the result of verification
 * 
 */
public class VerificationResult {

	/**
	 * Analysis concluded the code is safe
	 */
	public final boolean isSafe;

	// CONSTRUCTOR

	public VerificationResult(boolean isSafe) {
		this.isSafe = isSafe;
	}

	// UTILITY

	/**
	 * 
	 * @param expected
	 * @return a string that describes if this result matches the expected result
	 */
	public String compare(VerificationResult expected) {
		if (this.isSafe == expected.isSafe) {
			return "CORRECT";
		} else if (this.isSafe) {
			return "UNSOUND";
		} else {
			return "IMPRECISE";
		}
	}

	// CONVENIENCE: TOSTRING, EQUALS, HASHCODE

	@Override
	public String toString() {
		if (this.isSafe) {
			return Constants.safe;
		} else {
			return Constants.unsafe;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof VerificationResult)) {
			return false;
		}
		VerificationResult r = (VerificationResult) obj;
		return this.isSafe == r.isSafe;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.isSafe).toHashCode();
	}
};