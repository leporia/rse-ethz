package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START SAFE
// BEFORE_END SAFE

public class Basic_Test_Safe {
	public static void m1() {
		Event e = new Event(2, 4);
		e.switchLights(3);
	}
}