package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START SAFE
// BEFORE_END UNSAFE

public class Basic_Variable_Test_Unsafe_Before {
	public static void m1() {
		int k = 14;
		Event e = new Event(9, k*2);
		e.switchLights(69);
	}
}