package ch.ethz.rse.pointer;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import ch.ethz.rse.utils.Constants;
import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.spark.pag.Node;

/**
 * Convenience class which helps determine the {@link EventInitializer}s
 * potentially used to create objects pointed to by a given variable
 */
public class PointsToInitializer {

	private static final Logger logger = LoggerFactory.getLogger(PointsToInitializer.class);

	/**
	 * Internally used points-to analysis
	 */
	private final PointsToAnalysisWrapper pointsTo;

	/**
	 * class for which we are running points-to
	 */
	private final SootClass c;

	/**
	 * Maps abstract object indices to initializers
	 */
	private final Map<Node, EventInitializer> initializers = new HashMap<Node, EventInitializer>();

	/**
	 * All {@link EventInitializer}s, keyed by method
	 */
	private final Multimap<SootMethod, EventInitializer> perMethod = HashMultimap.create();

	/**
	 * All {@link JVirtualInvokeExpr}s, keyed by method
	 */
	private final Multimap<SootMethod, JVirtualInvokeExpr> virtualInvokes = HashMultimap.create();

	public PointsToInitializer(SootClass c) {
		this.c = c;
		logger.debug("Running points-to analysis on " + c.getName());
		this.pointsTo = new PointsToAnalysisWrapper(c);
		logger.debug("Analyzing initializers in " + c.getName());
		this.analyzeAllInitializers();
	}

	private void analyzeAllInitializers() {
		int idCounter = 0;
		for (SootMethod method : this.c.getMethods()) {

			if (method.getName().contains("<init>")) {
				// skip constructor of the class
				continue;
			}

			// populate data structures perMethod and initializers
			for (Unit u : method.getActiveBody().getUnits()) {
				if (!(u instanceof JInvokeStmt)) {
					continue;
				}

				JInvokeStmt invokeStmt = (JInvokeStmt) u;
				Value v = invokeStmt.getInvokeExpr();

				if (v instanceof JSpecialInvokeExpr) {
					// it is a class constructor call
					JSpecialInvokeExpr specialInvokeExpr = (JSpecialInvokeExpr) v;
					SootClass baseClass = specialInvokeExpr.getMethodRef().getDeclaringClass();

					if (!baseClass.getName().equals(Constants.EventClassName)) {
						continue;
					}

					// TODO
					// idk if this is the right way to get the node reference
					// probabily the right way is to get it directly from the object
					if (!(specialInvokeExpr.getBase() instanceof Local)) {
						throw new RuntimeException("Unexpected base class for special invoke expression: " + specialInvokeExpr);
					}
					Local base = (Local) specialInvokeExpr.getBase();
					// because there should be only one node after we define the object
					Node baseNode = this.pointsTo.getNodes(base).iterator().next();

					int start = ((IntConstant) specialInvokeExpr.getArg(0)).value;
					
					EventInitializer initializer = new EventInitializer(invokeStmt, idCounter, start);
					idCounter++;

					perMethod.put(method, initializer);
					initializers.put(baseNode, initializer);
				} else if (v instanceof JVirtualInvokeExpr) {
					// it is a call to an object
					JVirtualInvokeExpr virtualInvokeExpr = (JVirtualInvokeExpr) v;

					// check is the right class
					if (!(virtualInvokeExpr.getBase().getType().toString().equals(Constants.EventClassName))) {
						continue;
					}

					virtualInvokes.put(method, virtualInvokeExpr);
				}

			}

		}
	}

	// TODO: MAYBE FILL THIS OUT: add convenience methods

	public Collection<EventInitializer> getInitializers(SootMethod method) {
		return this.perMethod.get(method);
	}

	public Collection<JVirtualInvokeExpr> getVirtualInvokes(SootMethod method) {
		return this.virtualInvokes.get(method);
	}

	public List<EventInitializer> pointsTo(Local base) {
		Collection<Node> nodes = this.pointsTo.getNodes(base);
		List<EventInitializer> initializers = new LinkedList<EventInitializer>();
		for (Node node : nodes) {
			EventInitializer initializer = this.initializers.get(node);
			if (initializer != null) {
				// ignore nodes that were not initialized
				initializers.add(initializer);
			}
		}
		return initializers;
	}

	/**
	 * Returns all allocation nodes that could correspond to the given invokeExpression, which must be a call to Event init function
	 * Note that more than one node can be returned.
	 */
	public Collection<Node> getAllocationNodes(JSpecialInvokeExpr invokeExpr){
		if(!isRelevantInit(invokeExpr)){
			throw new RuntimeException("Call to getAllocationNodes with " + invokeExpr.toString() + "which is not an init call for the Event class");
		}
		Local base = (Local) invokeExpr.getBase();
		Collection<Node> allocationNodes = this.pointsTo.getNodes(base);
		return allocationNodes;
	}

	public boolean isRelevantInit(JSpecialInvokeExpr invokeExpr){
		Local base = (Local) invokeExpr.getBase();
		boolean isRelevant = base.getType().toString().equals(Constants.EventClassName);
		boolean isInit = invokeExpr.getMethod().getName().equals("<init>");
		return isRelevant && isInit;
	}
}
