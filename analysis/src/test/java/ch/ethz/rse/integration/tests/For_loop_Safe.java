package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START SAFE
// BEFORE_END SAFE

public class For_loop_Safe {
	public static void m1(int k) {

		Event e = new Event(0, 20);

		int a = 0;
		for (int i = 0; i < 10; i++) {
			e.switchLights(i);
			a = i;
		}
	}
}