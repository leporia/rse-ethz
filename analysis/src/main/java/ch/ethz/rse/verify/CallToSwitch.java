package ch.ethz.rse.verify;

import ch.ethz.rse.numerical.NumericalAnalysis;
import ch.ethz.rse.numerical.NumericalStateWrapper;
import soot.SootMethod;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JVirtualInvokeExpr;

/**
 * Convenience wrapper that stores information about a specific call to switchLights
 */
public class CallToSwitch {

	public final SootMethod method;
	public final NumericalAnalysis analysis;
	private final JInvokeStmt invokeStmt;

	public CallToSwitch(SootMethod method, NumericalAnalysis analysis,
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

	public JVirtualInvokeExpr getInvokeExpr() {
		return (JVirtualInvokeExpr) invokeStmt.getInvokeExpr();
	}

	public String toString() {
		return this.invokeStmt.toString();
	}
}
