package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START SAFE
// BEFORE_END SAFE

public class While_Test_Safe {
	public static void m1(int k) {

		if (k < 0) {
			k = 0;
		}

		Event e = new Event(0, k);

		while (k > 0) {
			k--;
			e.switchLights(k);
		}
	}
}