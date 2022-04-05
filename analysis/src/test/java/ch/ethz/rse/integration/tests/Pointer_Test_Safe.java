package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START SAFE
// BEFORE_END SAFE

public class Pointer_Test_Safe {
	public static void m1() {
		Event e = new Event(2, 5);
		Event foo = new Event(1, 2);
		foo = e;
		foo.switchLights(3);
	}
}