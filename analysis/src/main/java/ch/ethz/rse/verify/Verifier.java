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
				List<NumericalStateWrapper> brFlow = analysis.getBranchFlowAfter(init.getStatement());
				NumericalStateWrapper afterFlow = analysis.getFallFlowAfter(init.getStatement());

				Value end = init.getStatement().getInvokeExpr().getArg(1);
				int start = init.start;
				
				if (!(intValueDifference(start, end, afterFlow))) {
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

				Local baseLocal = (Local) base;
				List <EventInitializer> inits = pointsTo.pointsTo(baseLocal);
				Value time = invoke.getArg(0);

				for (EventInitializer init : inits) {
					List<NumericalStateWrapper> brFlow = analysis.getBranchFlowAfter(init.getStatement());
					NumericalStateWrapper afterFlow = analysis.getFallFlowAfter(init.getStatement());

					if (!(intValueDifference(init.start, time, afterFlow))) {
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

				Local baseLocal = (Local) base;
				List <EventInitializer> inits = pointsTo.pointsTo(baseLocal);
				Value time = invoke.getArg(0);

				for (EventInitializer init : inits) {
					List<NumericalStateWrapper> brFlow = analysis.getBranchFlowAfter(init.getStatement());
					NumericalStateWrapper afterFlow = analysis.getFallFlowAfter(init.getStatement());

					Abstract1 abstr = afterFlow.get();
					Environment env = abstr.getEnvironment();
					Manager man = abstr.getCreationManager();

					Value end = init.getStatement().getInvokeExpr().getArg(1);

					Texpr1Node timeNode;	
					Texpr1Node endNode;	

					if (time instanceof IntConstant) {
						timeNode = new Texpr1CstNode(new MpqScalar(((IntConstant) time).value));	
					} else if (time instanceof JimpleLocal) {
						String timeName = ((JimpleLocal) time).getName();
						timeNode = new Texpr1VarNode(timeName);
					} else {
						throw new RuntimeException("Unknown time value " + time);
					}

					if (end instanceof IntConstant) {
						endNode = new Texpr1CstNode(new MpqScalar(((IntConstant) end).value));	
					} else if (end instanceof JimpleLocal) {
						String endName = ((JimpleLocal) end).getName();
						endNode = new Texpr1VarNode(endName);
					} else {
						throw new RuntimeException("Unknown end value " + end);
					}

					// end - time
					Texpr1Node endMinusTime = new Texpr1BinNode(Texpr1BinNode.OP_SUB, Texpr1BinNode.RTYPE_INT, Texpr1BinNode.RDIR_ZERO, endNode, timeNode);
					logger.debug(endMinusTime.toString());
					
					// 0 <= end - time
					Tcons1 constraint = new Tcons1(env, Tcons1.SUPEQ, endMinusTime);
					logger.debug(constraint.toString());

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

	private boolean intValueDifference(int a, Value b, NumericalStateWrapper state) {
		if (b instanceof IntConstant) {
			int bInt = ((IntConstant) b).value;

			return a <= bInt;
		} else if (b instanceof JimpleLocal) {
			Abstract1 abstr = state.get();
			Environment env = abstr.getEnvironment();
			Manager man = abstr.getCreationManager();

			String bVar = ((JimpleLocal) b).getName();
			Texpr1Node bNode = new Texpr1VarNode(bVar);

			Texpr1CstNode aNode =  new Texpr1CstNode(new MpqScalar(a));

			// b - a
			Texpr1Node bMinusA = new Texpr1BinNode(Texpr1BinNode.OP_SUB, Texpr1BinNode.RTYPE_INT, Texpr1BinNode.RDIR_ZERO, bNode, aNode);
			
			// 0 <= b - a
			Tcons1 constraint = new Tcons1(env, Tcons1.SUPEQ, bMinusA);

			try {
				return abstr.satisfy(man, constraint);
			} catch (ApronException e) {
				return false;
			}

		} else {
			throw new RuntimeException("Unknown end value " + b);
		}
	}

}
