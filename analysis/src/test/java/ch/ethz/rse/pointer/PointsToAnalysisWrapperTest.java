package ch.ethz.rse.pointer;

import java.util.Collection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Iterators;

import ch.ethz.rse.VerificationProperty;
import ch.ethz.rse.testing.VerificationTestCase;
import soot.Body;
import soot.Local;
import soot.SootClass;
import soot.SootHelper;
import soot.jimple.spark.pag.Node;

/**
 * Sanity checks on points-to-analysis
 */
public class PointsToAnalysisWrapperTest {

	@Test
	public void testPointer() {
		String packageName = "ch.ethz.rse.integration.tests.Basic_Test_Safe";
		VerificationTestCase t = new VerificationTestCase(packageName, VerificationProperty.START_END_ORDER, true);
		SootClass sc = SootHelper.loadClassAndAnalyze(t.getTestClass());
		// run points-to analysis
		PointsToAnalysisWrapper w = new PointsToAnalysisWrapper(sc);

		// check that pointer indeed points to an abstract object
		Body b = sc.getMethodByName("m1").retrieveActiveBody();
		Local s = Iterators.get(b.getLocals().iterator(), 0);
		Collection<Node> pointsTo = w.getNodes(s);
		Assertions.assertEquals(1, pointsTo.size());
	}

}