package ch.ethz.rse.verify;

import ch.ethz.rse.numerical.NumericalAnalysis;
import ch.ethz.rse.numerical.NumericalStateWrapper;
import soot.SootMethod;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JSpecialInvokeExpr;

/**
 * Convenience wrapper that stores information about a specific Event init
 */
public class EventInit {

	public final SootMethod method;
	public final NumericalAnalysis analysis;
	private final JInvokeStmt invokeStmt;

	public EventInit(SootMethod method, NumericalAnalysis analysis,
	JInvokeStmt invokeStmt) {
		this.method = method;
		this.analysis = analysis;
		this.invokeStmt = invokeStmt;
	}

	public NumericalStateWrapper getStateBefore() {
		return this.analysis.getFlowBefore(this.invokeStmt);
	}

	public NumericalStateWrapper getStateAfter() {
		return this.analysis.getFallFlowAfter(this.invokeStmt);
	}

	public JSpecialInvokeExpr getInvokeExpr() {
		return (JSpecialInvokeExpr) invokeStmt.getInvokeExpr();
	}

	public String toString() {
		return this.invokeStmt.toString();
	}
}
