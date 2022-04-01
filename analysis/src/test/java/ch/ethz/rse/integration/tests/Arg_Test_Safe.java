package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER UNSAFE
// AFTER_START SAFE
// BEFORE_END UNSAFE

public class Arg_Test_Safe {
	public static void m1(int k) {
		int y = 13;
		int w = k*9 + y;
		Event e = new Event(2, w-5);
		e.switchLights(3);
	}
}