package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START SAFE
// BEFORE_END SAFE

public class Basic_Variable_Test_Safe {
	public static void m1() {
		int i = 2;
		Event e = new Event(3, i*2);
		e.switchLights(3);
	}
}