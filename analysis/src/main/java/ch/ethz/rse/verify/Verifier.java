package ch.ethz.rse.verify;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import apron.MpqScalar;
import apron.Texpr1CstNode;
import apron.Texpr1Node;
import apron.Texpr1VarNode;
import ch.ethz.rse.VerificationProperty;
import ch.ethz.rse.numerical.NumericalAnalysis;
import ch.ethz.rse.numerical.NumericalStateWrapper;
import ch.ethz.rse.pointer.EventInitializer;
import ch.ethz.rse.pointer.PointsToInitializer;
import ch.ethz.rse.utils.Constants;
import polyglot.ast.Call;
import soot.Local;
import soot.SootClass;
import soot.SootHelper;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.IntConstant;
import soot.toolkits.graph.UnitGraph;

/**
 * Main class handling verification
 * 
 */
public class Verifier extends AVerifier {

	private static final Logger logger = LoggerFactory.getLogger(Verifier.class);

	/**
	 * class to be verified
	 */
	private final SootClass c;

	/**
	 * points to analysis for verified class
	 */
	private final PointsToInitializer pointsTo;

	/**
	 * list of already analyzed properties
	 */
	private Set<VerificationProperty> analyzedProperties;

	/**
	 * 
	 * @param c class to verify
	 */
	public Verifier(SootClass c) {
		logger.debug("Analyzing {}", c.getName());

		this.c = c;
		// initialize analyzedProperty set
		this.analyzedProperties = new HashSet<VerificationProperty>();

		// pointer analysis
		this.pointsTo = new PointsToInitializer(this.c);
	}

	protected void runNumericalAnalysis(VerificationProperty property) {
		analyzedProperties.add(property);

		// iterate over all methods of c
		for (SootMethod m : c.getMethods()) {
			if (m.isAbstract() || m.isNative() || m.isPhantom()) {
				continue;
			}

			logger.debug("Analyzing method {}", m.getName());

			// run numerical analysis
			NumericalAnalysis analysis = new NumericalAnalysis(m, property, this.pointsTo);
			this.numericalAnalysis.put(m, analysis);
		}
	}

	@Override
	public boolean checkStartEndOrder() {
		if (!analyzedProperties.contains(VerificationProperty.START_END_ORDER)) {
			return false;
		}

		// iterate over all analyzed methods
		for (SootMethod m : this.numericalAnalysis.keySet()) {
			NumericalAnalysis analysis = this.numericalAnalysis.get(m);

			// iterate over all units of method m
			for (Unit u : m.getActiveBody().getUnits()) {
				//logger.debug(u.toString());

				if (!(u instanceof JInvokeStmt)) {
					continue;
				}

				JInvokeStmt invokeStmt = (JInvokeStmt) u;
				Value v = invokeStmt.getInvokeExpr();

				if (!(v instanceof JSpecialInvokeExpr)) {
					continue;
				}

				JSpecialInvokeExpr specialInvokeExpr = (JSpecialInvokeExpr) v;
				SootClass baseClass = specialInvokeExpr.getMethodRef().getDeclaringClass();

				if (!baseClass.getName().equals(Constants.EventClassName)) {
					continue;
				}

				// first argument is always an int constant
				int start = ((IntConstant) specialInvokeExpr.getArg(0)).value;

				Value end = specialInvokeExpr.getArg(1);

				if (!(end instanceof IntConstant)) {
					logger.error("Unimplemented: End of event is not an int constant");
					continue;
				}

				int end_int = ((IntConstant) end).value;

				if (start > end_int) {
					return false;
				}
			}
		}

		// TODO: FILL THIS OUT
		return true;
	}

	@Override
	public boolean checkAfterStart() {
		if (!analyzedProperties.contains(VerificationProperty.AFTER_START)) {
			return false;
		}

		// TODO: FILL THIS OUT
		return true;
	}

	@Override
	public boolean checkBeforeEnd() {
		if (!analyzedProperties.contains(VerificationProperty.BEFORE_END)) {
			return false;
		}

		// TODO: FILL THIS OUT
		return true;
	}

	// TODO: MAYBE FILL THIS OUT: add convenience methods

}
