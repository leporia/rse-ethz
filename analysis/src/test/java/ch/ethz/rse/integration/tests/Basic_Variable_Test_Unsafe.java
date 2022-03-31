package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER UNSAFE
// AFTER_START UNSAFE
// BEFORE_END UNSAFE

public class Basic_Variable_Test_Unsafe {
	public static void m1() {
		int k = 2;
		Event e = new Event(9, k*2);
		e.switchLights(3);
	}
}