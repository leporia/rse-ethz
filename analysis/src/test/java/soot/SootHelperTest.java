package soot;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ch.ethz.rse.VerificationProperty;
import ch.ethz.rse.testing.VerificationTestCase;
import ch.ethz.rse.verify.ClassToVerify;

/**
 * Sanity check on classes loaded by soot
 */
public class SootHelperTest {

	public ClassToVerify getExampleClassToVerify() {
		String packageName = "ch.ethz.rse.integration.tests.Basic_Test_Safe";
		VerificationTestCase c = new VerificationTestCase(packageName, VerificationProperty.START_END_ORDER, true);
		return c.getTestClass();
	}

	@Test
	public void testLoad() {
		ClassToVerify c = this.getExampleClassToVerify();
		SootClass sc = SootHelper.loadClass(c);

		// extract methods
		List<SootMethod> methods = sc.getMethods();
		// expect both init method and m1
		Assertions.assertEquals(2, methods.size());

		// check method name
		SootMethod method = methods.get(1);
		Assertions.assertEquals("m1", method.getName());

		// check method body
		Body body = method.retrieveActiveBody();
		Assertions.assertNotNull(body);
	}

	@Test
	public void testLoadAndAnalyze() {
		ClassToVerify c = this.getExampleClassToVerify();
		SootHelper.loadClassAndAnalyze(c);

		PointsToAnalysis a = Scene.v().getPointsToAnalysis();
		Assertions.assertNotNull(a);
	}

}