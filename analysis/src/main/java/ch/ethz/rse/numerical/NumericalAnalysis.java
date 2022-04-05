package ch.ethz.rse.numerical;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.RuntimeErrorException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import apron.Abstract1;
import apron.ApronException;
import apron.Environment;
import apron.Interval;
import apron.Manager;
import apron.MpqScalar;
import apron.Polka;
import apron.Tcons1;
import apron.Texpr1BinNode;
import apron.Texpr1CstNode;
import apron.Texpr1Intern;
import apron.Texpr1Node;
import apron.Texpr1VarNode;
import apron.Var;
import ch.ethz.rse.VerificationProperty;
import ch.ethz.rse.pointer.EventInitializer;
import ch.ethz.rse.pointer.PointsToInitializer;
import ch.ethz.rse.utils.Constants;
import ch.ethz.rse.verify.EnvironmentGenerator;
import soot.ArrayType;
import soot.DoubleType;
import soot.Local;
import soot.RefType;
import soot.SootHelper;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AddExpr;
import soot.jimple.BinopExpr;
import soot.jimple.ConditionExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.MulExpr;
import soot.jimple.ParameterRef;
import soot.jimple.Stmt;
import soot.jimple.SubExpr;
import soot.jimple.internal.AbstractBinopExpr;
import soot.jimple.internal.JAddExpr;
import soot.jimple.internal.JArrayRef;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JGeExpr;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JGtExpr;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JLeExpr;
import soot.jimple.internal.JLtExpr;
import soot.jimple.internal.JMulExpr;
import soot.jimple.internal.JNeExpr;
import soot.jimple.internal.JReturnVoidStmt;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.jimple.internal.JSubExpr;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardBranchedFlowAnalysis;

/**
 * Convenience class running a numerical analysis on a given {@link SootMethod}
 */
public class NumericalAnalysis extends ForwardBranchedFlowAnalysis<NumericalStateWrapper> {

	private static final Logger logger = LoggerFactory.getLogger(NumericalAnalysis.class);

	/**
	 * the property we are verifying
	 */
	private final VerificationProperty property;

	/**
	 * the pointer analysis result we are verifying
	 */
	private final PointsToInitializer pointsTo;

	/**
	 * all event initializers encountered until now
	 */
	private Set<EventInitializer> alreadyInit;

	/**
	 * number of times this loop head was encountered during analysis
	 */
	private HashMap<Unit, IntegerWrapper> loopHeads = new HashMap<Unit, IntegerWrapper>();
	/**
	 * Previously seen abstract state for each loop head
	 */
	private HashMap<Unit, NumericalStateWrapper> loopHeadState = new HashMap<Unit, NumericalStateWrapper>();

	/**
	 * Numerical abstract domain to use for analysis: Convex polyhedra
	 */
	public final Manager man = new Polka(true);

	public final Environment env;

	/**
	 * We apply widening after updating the state at a given merge point for the
	 * {@link WIDENING_THRESHOLD}th time
	 */
	private static final int WIDENING_THRESHOLD = 6;

	/**
	 * 
	 * @param method   method to analyze
	 * @param property the property we are verifying
	 */
	public NumericalAnalysis(SootMethod method, VerificationProperty property, PointsToInitializer pointsTo) {
		super(SootHelper.getUnitGraph(method));

		UnitGraph g = SootHelper.getUnitGraph(method);

		this.property = property;

		this.pointsTo = pointsTo;

		this.alreadyInit = new HashSet<EventInitializer>();

		this.env = new EnvironmentGenerator(method, pointsTo).getEnvironment();

		// initialize counts for loop heads
		for (Loop l : new LoopNestTree(g.getBody())) {
			loopHeads.put(l.getHead(), new IntegerWrapper(0));
		}

		// perform analysis by calling into super-class
		logger.info("Analyzing {} in {}", method.getName(), method.getDeclaringClass().getName());
		doAnalysis(); // calls newInitialFlow, entryInitialFlow, merge, flowThrough, and stops when a fixed point is reached
	}

	/**
	 * Report unhandled instructions, types, cases, etc.
	 * 
	 * @param task description of current task
	 * @param what
	 */
	public static void unhandled(String task, Object what, boolean raiseException) {
		String description = task + ": Can't handle " + what.toString() + " of type " + what.getClass().getName();

		if (raiseException) {
			logger.error("Raising exception " + description);
			throw new UnsupportedOperationException(description);
		} else {
			logger.error(description);

			// print stack trace
			StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			for (int i = 1; i < stackTrace.length; i++) {
				logger.error(stackTrace[i].toString());
			}
		}
	}

	@Override
	protected void copy(NumericalStateWrapper source, NumericalStateWrapper dest) {
		source.copyInto(dest);
	}

	@Override
	protected NumericalStateWrapper newInitialFlow() {
		// should be bottom (only entry flows are not bottom originally)
		return NumericalStateWrapper.bottom(man, env);
	}

	@Override
	protected NumericalStateWrapper entryInitialFlow() {
		// state of entry points into function
		NumericalStateWrapper ret = NumericalStateWrapper.top(man, env);

		// TODO: MAYBE FILL THIS OUT

		return ret;
	}

	@Override
	protected void merge(Unit succNode, NumericalStateWrapper w1, NumericalStateWrapper w2, NumericalStateWrapper w3) {
		// merge the two states from w1 and w2 and store the result into w3
		logger.debug("in merge: " + succNode);

		// TODO not sure if this is correct
		Abstract1 a1 = w1.get();
		Abstract1 a2 = w2.get();

		try {
			a1.join(man, a2);
		} catch (ApronException e) {
			e.printStackTrace();
			throw new RuntimeException("should not be here");
		}

		w3.set(a1);
	}

	@Override
	protected void merge(NumericalStateWrapper src1, NumericalStateWrapper src2, NumericalStateWrapper trg) {
		// this method is never called, we are using the other merge instead
		throw new UnsupportedOperationException();
	}

	@Override
	protected void flowThrough(NumericalStateWrapper inWrapper, Unit op, List<NumericalStateWrapper> fallOutWrappers,
			List<NumericalStateWrapper> branchOutWrappers) {
		logger.debug(inWrapper + " " + op + " => ?");

		Stmt s = (Stmt) op;

		// fallOutWrapper is the wrapper for the state after running op,
		// assuming we move to the next statement. Do not overwrite
		// fallOutWrapper, but use its .set method instead
		assert fallOutWrappers.size() <= 1;
		NumericalStateWrapper fallOutWrapper = null;
		if (fallOutWrappers.size() == 1) {
			fallOutWrapper = fallOutWrappers.get(0);
			inWrapper.copyInto(fallOutWrapper);
		}

		// branchOutWrapper is the wrapper for the state after running op,
		// assuming we follow a conditional jump. It is therefore only relevant
		// if op is a conditional jump. In this case, (i) fallOutWrapper
		// contains the state after "falling out" of the statement, i.e., if the
		// condition is false, and (ii) branchOutWrapper contains the state
		// after "branching out" of the statement, i.e., if the condition is
		// true.
		assert branchOutWrappers.size() <= 1;
		NumericalStateWrapper branchOutWrapper = null;
		if (branchOutWrappers.size() == 1) {
			branchOutWrapper = branchOutWrappers.get(0);
			inWrapper.copyInto(branchOutWrapper);
		}

		try {
			if (s instanceof DefinitionStmt) {
				// handle assignment

				DefinitionStmt sd = (DefinitionStmt) s;
				Value left = sd.getLeftOp();
				Value right = sd.getRightOp();

				// We are not handling these cases:
				if (!(left instanceof JimpleLocal)) {
					unhandled("Assignment to non-local variable", left, true);
				} else if (left instanceof JArrayRef) {
					unhandled("Assignment to a non-local array variable", left, true);
				} else if (left.getType() instanceof ArrayType) {
					unhandled("Assignment to Array", left, true);
				} else if (left.getType() instanceof DoubleType) {
					unhandled("Assignment to double", left, true);
				} else if (left instanceof JInstanceFieldRef) {
					unhandled("Assignment to field", left, true);
				}

				if (left.getType() instanceof RefType) {
					// assignments to references are handled by pointer analysis
					// no action necessary
				} else {
					// handle assignment
					handleDef(fallOutWrapper, left, right);
				}

			} else if (s instanceof JIfStmt) {
				// handle if

				JIfStmt ifStmt = (JIfStmt) s;
				Value condition = ifStmt.getCondition();
				Tcons1[] constrs = compileCondition(condition);
				Tcons1 cons = constrs[0];
				Tcons1 invCons = constrs[1];
				Abstract1 branchIn = new Abstract1(man, new Tcons1[]{cons});
				Abstract1 branchOut = new Abstract1(man, new Tcons1[]{invCons});

				// case if is false then skip
				Abstract1 fallOut = fallOutWrapper.get();
				fallOut.meet(man, branchOut);
				fallOutWrapper.set(fallOut);

				// case if is true then enter branch
				Abstract1 branch = branchOutWrapper.get();
				branch.meet(man, branchIn);
				branchOutWrapper.set(branch);

			} else if (s instanceof JInvokeStmt) {
				// handle invocations
				JInvokeStmt jInvStmt = (JInvokeStmt) s;
				InvokeExpr invokeExpr = jInvStmt.getInvokeExpr();
				if (invokeExpr instanceof JVirtualInvokeExpr) {
					handleInvoke(jInvStmt, fallOutWrapper);
				} else if (invokeExpr instanceof JSpecialInvokeExpr) {
					// initializer for object
					handleInitialize(jInvStmt, fallOutWrapper);
				} else {
					unhandled("Unhandled invoke statement", invokeExpr, true);
				}
			} else if (s instanceof JGotoStmt) {
				// safe to ignore
			} else if (s instanceof JReturnVoidStmt) {
				// safe to ignore
			} else {
				unhandled("Unhandled statement", s, true);
			}

			// log outcome
			if (fallOutWrapper != null) {
				logger.debug(inWrapper.get() + " " + s + " =>[fallout] " + fallOutWrapper);
			}
			if (branchOutWrapper != null) {
				logger.debug(inWrapper.get() + " " + s + " =>[branchout] " + branchOutWrapper);
			}

		} catch (ApronException e) {
			throw new RuntimeException(e);
		}
	}

	public void handleInvoke(JInvokeStmt jInvStmt, NumericalStateWrapper fallOutWrapper) throws ApronException {
		// TODO: MAYBE FILL THIS OUT
	}

	public void handleInitialize(JInvokeStmt jInvStmt, NumericalStateWrapper fallOutWrapper) throws ApronException {
		// TODO: MAYBE FILL THIS OUT
	}

	// returns state of in after assignment
	private void handleDef(NumericalStateWrapper outWrapper, Value left, Value right) throws ApronException {
		// skip function paramenters because they are unknown
		if (right instanceof ParameterRef) {
			return;
		}

		Abstract1 curr = outWrapper.get();

		String leftName = ((JimpleLocal) left).getName();
		Texpr1Intern rightIntern = new Texpr1Intern(env, compileExpression(right));

		Abstract1 next = curr.assignCopy(man, leftName, rightIntern, curr);
		outWrapper.set(next);
	}

	private Texpr1Node compileExpression(Value expr) {
		if (expr instanceof IntConstant) {
			int intValue = ((IntConstant) expr).value;
			return new Texpr1CstNode(new MpqScalar(intValue));
		} else if (expr instanceof JimpleLocal) {
			String name = ((JimpleLocal) expr).getName();
			return new Texpr1VarNode(name);
		} else if (expr instanceof JMulExpr) {
			JMulExpr mulExpr = (JMulExpr) expr;
			Texpr1Node op1 = compileExpression(mulExpr.getOp1());
			Texpr1Node op2 = compileExpression(mulExpr.getOp2());
         	return new Texpr1BinNode(Texpr1BinNode.OP_MUL, Texpr1BinNode.RTYPE_INT, Texpr1BinNode.RDIR_ZERO, op1, op2);
		} else if (expr instanceof JAddExpr) {
			JAddExpr addExpr = (JAddExpr) expr;
			Texpr1Node op1 = compileExpression(addExpr.getOp1());
			Texpr1Node op2 = compileExpression(addExpr.getOp2());
			return new Texpr1BinNode(Texpr1BinNode.OP_ADD, Texpr1BinNode.RTYPE_INT, Texpr1BinNode.RDIR_ZERO, op1, op2);
		} else if (expr instanceof JSubExpr) {
			JSubExpr subExpr = (JSubExpr) expr;
			Texpr1Node op1 = compileExpression(subExpr.getOp1());
			Texpr1Node op2 = compileExpression(subExpr.getOp2());
			return new Texpr1BinNode(Texpr1BinNode.OP_SUB, Texpr1BinNode.RTYPE_INT, Texpr1BinNode.RDIR_ZERO, op1, op2);
		} else {
			throw new RuntimeException("Unhandled expression: " + expr.toString());
		}
	}

	// return constrain and ~constrain in a size 2 array
	private Tcons1[] compileCondition(Value expr) {
		if (expr instanceof JEqExpr) {
			// a == b
			JEqExpr eqExpr = (JEqExpr) expr;
			Texpr1Node op1 = compileExpression(eqExpr.getOp1());
			Texpr1Node op2 = compileExpression(eqExpr.getOp2());
			Texpr1Node result = new Texpr1BinNode(Texpr1BinNode.OP_SUB, Texpr1BinNode.RTYPE_INT, Texpr1BinNode.RDIR_ZERO, op1, op2);

			Tcons1 cons = new Tcons1(env, Tcons1.EQ, result);
			Tcons1 invCons = new Tcons1(env, Tcons1.DISEQ, result);

			return new Tcons1[] { cons, invCons };
		} else if (expr instanceof JNeExpr) {
			// a != b
			JNeExpr neExpr = (JNeExpr) expr;
			Texpr1Node op1 = compileExpression(neExpr.getOp1());
			Texpr1Node op2 = compileExpression(neExpr.getOp2());
			Texpr1Node result = new Texpr1BinNode(Texpr1BinNode.OP_SUB, Texpr1BinNode.RTYPE_INT, Texpr1BinNode.RDIR_ZERO, op1, op2);

			Tcons1 cons = new Tcons1(env, Tcons1.DISEQ, result);
			Tcons1 invCons = new Tcons1(env, Tcons1.EQ, result);

			return new Tcons1[] { cons, invCons };
		} else if (expr instanceof JGtExpr) {
			// a > b
			JGtExpr gtExpr = (JGtExpr) expr;
			Texpr1Node op1 = compileExpression(gtExpr.getOp1());
			Texpr1Node op2 = compileExpression(gtExpr.getOp2());

			// a - b
			// b - a
			Texpr1Node result = new Texpr1BinNode(Texpr1BinNode.OP_SUB, Texpr1BinNode.RTYPE_INT, Texpr1BinNode.RDIR_ZERO, op1, op2);
			Texpr1Node invResult = new Texpr1BinNode(Texpr1BinNode.OP_SUB, Texpr1BinNode.RTYPE_INT, Texpr1BinNode.RDIR_ZERO, op2, op1);

			// a - b > 0
			// 0 <= b -a
			Tcons1 cons = new Tcons1(env, Tcons1.SUP, result);
			Tcons1 invCons = new Tcons1(env, Tcons1.SUPEQ, invResult);

			return new Tcons1[] { cons, invCons };
		} else if (expr instanceof JGeExpr) {
			// a >= b
			JGeExpr geExpr = (JGeExpr) expr;
			Texpr1Node op1 = compileExpression(geExpr.getOp1());
			Texpr1Node op2 = compileExpression(geExpr.getOp2());

			// a - b
			// b - a
			Texpr1Node result = new Texpr1BinNode(Texpr1BinNode.OP_SUB, Texpr1BinNode.RTYPE_INT, Texpr1BinNode.RDIR_ZERO, op1, op2);
			Texpr1Node invResult = new Texpr1BinNode(Texpr1BinNode.OP_SUB, Texpr1BinNode.RTYPE_INT, Texpr1BinNode.RDIR_ZERO, op2, op1);

			// a - b >= 0
			// 0 < b - a
			Tcons1 cons = new Tcons1(env, Tcons1.SUPEQ, result);
			Tcons1 invCons = new Tcons1(env, Tcons1.SUP, invResult);

			return new Tcons1[] { cons, invCons };
		} else if (expr instanceof JLtExpr) {
			// a < b
			JLtExpr ltExpr = (JLtExpr) expr;
			Texpr1Node op1 = compileExpression(ltExpr.getOp1());
			Texpr1Node op2 = compileExpression(ltExpr.getOp2());

			// b - a
			// a - b
			Texpr1Node result = new Texpr1BinNode(Texpr1BinNode.OP_SUB, Texpr1BinNode.RTYPE_INT, Texpr1BinNode.RDIR_ZERO, op2, op1);
			Texpr1Node invResult = new Texpr1BinNode(Texpr1BinNode.OP_SUB, Texpr1BinNode.RTYPE_INT, Texpr1BinNode.RDIR_ZERO, op1, op2);

			// 0 < b - a
			// a - b < 0
			Tcons1 cons = new Tcons1(env, Tcons1.SUP, result);
			Tcons1 invCons = new Tcons1(env, Tcons1.SUPEQ, invResult);

			return new Tcons1[] { cons, invCons };
		} else if (expr instanceof JLeExpr) {
			// a <= b
			JLeExpr leExpr = (JLeExpr) expr;
			Texpr1Node op1 = compileExpression(leExpr.getOp1());
			Texpr1Node op2 = compileExpression(leExpr.getOp2());

			// b - a
			// a - b
			Texpr1Node result = new Texpr1BinNode(Texpr1BinNode.OP_SUB, Texpr1BinNode.RTYPE_INT, Texpr1BinNode.RDIR_ZERO, op2, op1);
			Texpr1Node invResult = new Texpr1BinNode(Texpr1BinNode.OP_SUB, Texpr1BinNode.RTYPE_INT, Texpr1BinNode.RDIR_ZERO, op1, op2);

			// 0 <= b - a
			// a - b > 0
			Tcons1 cons = new Tcons1(env, Tcons1.SUPEQ, result);
			Tcons1 invCons = new Tcons1(env, Tcons1.SUP, invResult);

			return new Tcons1[] { cons, invCons };
		} else {
			throw new RuntimeException("Unhandled condition: " + expr.toString());
		}
	}
}
