package ch.ethz.rse.numerical;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import apron.Abstract1;
import apron.ApronException;
import apron.Environment;
import apron.Interval;
import apron.Manager;
import apron.NotImplementedException;
import apron.Tcons1;
import apron.Texpr1Intern;
import soot.Local;
import soot.SootHelper;
import soot.Value;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;

/**
 * Convenience wrapper for numerical abstract elements in Apron.
 * 
 */
public class NumericalStateWrapper {

	private static final Logger logger = LoggerFactory.getLogger(NumericalStateWrapper.class);

	// STATIC

	public static NumericalStateWrapper bottom(Manager man, Environment env) {
		try {
			Abstract1 bot = new Abstract1(man, env, true);
			return new NumericalStateWrapper(man, bot);
		} catch (ApronException e) {
			throw new RuntimeException(e);
		}
	}

	public static NumericalStateWrapper top(Manager man, Environment env) {
		try {
			Abstract1 top = new Abstract1(man, env);
			return new NumericalStateWrapper(man, top);
		} catch (ApronException e) {
			throw new RuntimeException(e);
		}
	}

	// FIELDS

	/**
	 * Wrapped abstract element
	 */
	private Abstract1 elem;

	/**
	 * Manager for numerical abstract domain
	 */
	private final Manager man;

	// CONSTRUCTOR

	/**
	 * 
	 * @param man  Apron abstract domain manager
	 * @param elem Abstract Apron element
	 */
	public NumericalStateWrapper(Manager man, Abstract1 elem) {
		this.man = man;
		this.elem = elem;
	}

	// FUNCTIONS

	public Abstract1 get() {
		return elem;
	}

	public void set(Abstract1 e) {
		elem = e;
	}

	public NumericalStateWrapper copy() {
		Abstract1 copy;
		try {
			copy = new Abstract1(man, this.elem);
			return new NumericalStateWrapper(this.man, copy);
		} catch (ApronException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Copies this state into `other`
	 * 
	 * @param other
	 */
	public void copyInto(NumericalStateWrapper other) {
		NumericalStateWrapper copy = this.copy();
		other.elem = copy.elem;
	}

	// TODO: MAYBE FILL THIS OUT: add convenience methods

	// EQUALS, HASHCODE, TOSTRING

	@Override
	public boolean equals(Object o) {
		// needed by NumericalAnalysis
		if (!(o instanceof NumericalStateWrapper)) {
			return false;
		}
		NumericalStateWrapper w = (NumericalStateWrapper) o;

		Abstract1 t = w.get();
		try {
			// sanity check
			if (elem.isEqual(man, t) && !elem.isIncluded(man, t)) {
				throw new RuntimeException("VIOLATION");
			}

			return elem.isEqual(man, t);
		} catch (ApronException e) {
			throw new RuntimeException("isEqual failed");
		}
	}

	@Override
	public int hashCode() {
		// implementation non-trivial but not needed
		throw new RuntimeException(new NotImplementedException());
	}

	@Override
	public String toString() {
		try {
			if (elem == null) {
				return "null";
			} else if (elem.isTop(man)) {
				return "<Top>";
			} else {
				return elem.toString();
			}
		} catch (ApronException e) {
			throw new RuntimeException(e);
		}
	}
}
