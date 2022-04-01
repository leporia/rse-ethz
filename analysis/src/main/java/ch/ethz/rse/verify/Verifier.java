package ch.ethz.rse.verify;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import apron.MpqScalar;
import apron.Tcons1;
import apron.Texpr1BinNode;
import apron.Texpr1CstNode;
import apron.Texpr1Node;
import apron.Texpr1VarNode;
import apron.Abstract1;
import apron.ApronException;
import apron.Environment;
import apron.Manager;
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
import soot.jimple.internal.JimpleLocal;
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

			for (EventInitializer init : pointsTo.getInitializers(m)) {
				List<NumericalStateWrapper> br_flow = analysis.getBranchFlowAfter(init.getStatement());
				NumericalStateWrapper after_flow = analysis.getFallFlowAfter(init.getStatement());

				Value end = init.getStatement().getInvokeExpr().getArg(1);
				int start = init.start;
				
				if (!(intValueDifference(start, end, after_flow))) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public boolean checkAfterStart() {
		if (!analyzedProperties.contains(VerificationProperty.AFTER_START)) {
			return false;
		}

		for (SootMethod m : this.numericalAnalysis.keySet()) {
			NumericalAnalysis analysis = this.numericalAnalysis.get(m);

			for (JVirtualInvokeExpr invoke : pointsTo.getVirtualInvokes(m)) {
				Value base = invoke.getBase();
				if (!(base instanceof Local)) {
					throw new RuntimeException("Unknown base value " + base);
				}

				Local base_local = (Local) base;
				List <EventInitializer> inits = pointsTo.pointsTo(base_local);
				Value time = invoke.getArg(0);

				for (EventInitializer init : inits) {
					List<NumericalStateWrapper> br_flow = analysis.getBranchFlowAfter(init.getStatement());
					NumericalStateWrapper after_flow = analysis.getFallFlowAfter(init.getStatement());

					if (!(intValueDifference(init.start, time, after_flow))) {
						return false;
					}
				}
			}
		}

		return true;
	}

	@Override
	public boolean checkBeforeEnd() {
		if (!analyzedProperties.contains(VerificationProperty.BEFORE_END)) {
			return false;
		}

		for (SootMethod m : this.numericalAnalysis.keySet()) {
			NumericalAnalysis analysis = this.numericalAnalysis.get(m);

			for (JVirtualInvokeExpr invoke : pointsTo.getVirtualInvokes(m)) {
				Value base = invoke.getBase();
				if (!(base instanceof Local)) {
					throw new RuntimeException("Unknown base value " + base);
				}

				Local base_local = (Local) base;
				List <EventInitializer> inits = pointsTo.pointsTo(base_local);
				Value time = invoke.getArg(0);

				for (EventInitializer init : inits) {
					List<NumericalStateWrapper> br_flow = analysis.getBranchFlowAfter(init.getStatement());
					NumericalStateWrapper after_flow = analysis.getFallFlowAfter(init.getStatement());

					Abstract1 abstr = after_flow.get();
					Environment env = abstr.getEnvironment();
					Manager man = abstr.getCreationManager();

					Value end = init.getStatement().getInvokeExpr().getArg(1);

					Texpr1Node time_node;	
					Texpr1Node end_node;	

					if (time instanceof IntConstant) {
						time_node = new Texpr1CstNode(new MpqScalar(((IntConstant) time).value));	
					} else if (time instanceof JimpleLocal) {
						String time_name = ((JimpleLocal) time).getName();
						time_node = new Texpr1VarNode(time_name);
					} else {
						throw new RuntimeException("Unknown time value " + time);
					}

					if (end instanceof IntConstant) {
						end_node = new Texpr1CstNode(new MpqScalar(((IntConstant) end).value));	
					} else if (end instanceof JimpleLocal) {
						String end_name = ((JimpleLocal) end).getName();
						end_node = new Texpr1VarNode(end_name);
					} else {
						throw new RuntimeException("Unknown end value " + end);
					}

					// end - time
					Texpr1Node end_minus_time = new Texpr1BinNode(Texpr1BinNode.OP_SUB, Texpr1BinNode.RTYPE_INT, Texpr1BinNode.RDIR_ZERO, end_node, time_node);
					
					// 0 <= end - time
					Tcons1 constraint = new Tcons1(env, Tcons1.SUPEQ, end_minus_time);

					try {
						if (!abstr.satisfy(man, constraint)) {
							return false;
						}
					} catch (ApronException e) {
						return false;
					}

				}
			}
		}

		return true;
	}

	// TODO: MAYBE FILL THIS OUT: add convenience methods

	private boolean intValueDifference(int a, Value b, NumericalStateWrapper state) {
		if (b instanceof IntConstant) {
			int b_int = ((IntConstant) b).value;

			return a <= b_int;
		} else if (b instanceof JimpleLocal) {
			Abstract1 abstr = state.get();
			Environment env = abstr.getEnvironment();
			Manager man = abstr.getCreationManager();

			String b_var = ((JimpleLocal) b).getName();
			Texpr1Node b_node = new Texpr1VarNode(b_var);

			Texpr1CstNode a_node =  new Texpr1CstNode(new MpqScalar(a));

			// b - a
			Texpr1Node b_minus_a = new Texpr1BinNode(Texpr1BinNode.OP_SUB, Texpr1BinNode.RTYPE_INT, Texpr1BinNode.RDIR_ZERO, b_node, a_node);
			
			// 0 <= b - a
			Tcons1 constraint = new Tcons1(env, Tcons1.SUPEQ, b_minus_a);

			try {
				return abstr.satisfy(man, constraint);
			} catch (ApronException e) {
				return false;
			}

		} else {
			// TODO we have to support arrays? or other things?
			throw new RuntimeException("Unknown end value " + b);
		}
	}

}
