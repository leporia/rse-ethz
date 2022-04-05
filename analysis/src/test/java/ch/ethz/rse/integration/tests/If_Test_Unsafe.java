package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER UNSAFE
// AFTER_START SAFE
// BEFORE_END UNSAFE

public class If_Test_Unsafe {
	public static void m1(int k) {
		if (k < 2) {
			k = 1;
		}

		Event e = new Event(2, k);
		e.switchLights(3);
	}
}