package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START SAFE
// BEFORE_END SAFE

public class Branch_Test_Safe {
	public static void m1(int k, int v) {
		if (k < 2) {
			k = 2;
		}

		if (k > 10) {
			k = 10;
		}

		if (v <= 1) {
			v = 2;
		}

		if (v >= k) {
			v = k;
		}

		if (k == 2 && v != 2) {
			k = 3;
			v = 2;
		}

		Event e = new Event(2, k);
		e.switchLights(v);
	}
}