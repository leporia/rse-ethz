package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER UNSAFE
// AFTER_START UNSAFE
// BEFORE_END UNSAFE

public class Basic_Variable_Test_Unsafe_Before {
	public static void m1() {
		int k = 14;
		int x = 92;
		// end = 200
		Event e = new Event(360, (x-6*k+2)*2*(k-5+1));
		e.switchLights(220);
	}
}