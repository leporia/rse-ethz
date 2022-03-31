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

				if (end instanceof IntConstant) {
					int end_int = ((IntConstant) end).value;

					return start <= end_int;
				} else if (end instanceof JimpleLocal) {
					Abstract1 abstr = after_flow.get();
					Environment env = abstr.getEnvironment();
					Manager man = abstr.getCreationManager();

					String end_var = ((JimpleLocal) end).getName();
					Texpr1Node end_node = new Texpr1VarNode(end_var);

					Texpr1CstNode start_node =  new Texpr1CstNode(new MpqScalar(start));

					// end - start
					Texpr1Node end_minus_start = new Texpr1BinNode(Texpr1BinNode.OP_SUB, Texpr1BinNode.RTYPE_INT, Texpr1BinNode.RDIR_ZERO, end_node, start_node);
					
					// 0 <= end - start 
					Tcons1 constraint = new Tcons1(env, Tcons1.SUPEQ, end_minus_start);

					try {
						return abstr.satisfy(man, constraint);
					} catch (ApronException e) {
						return false;
					}

				} else {
					// TODO we have to support arrays? or other things?
					logger.error("Unknown end value {}", end);
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
