package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START SAFE
// BEFORE_END SAFE

public class NotEq_Test_Safe {
	public static void m1(int k) {
		if (k != 4) {
			k = 4;
		}

		Event e = new Event(2, k);
		e.switchLights(3);
	}
}