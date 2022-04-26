package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START SAFE
// BEFORE_END SAFE

public class Widening_Test_Safe {
	public static void m1() {

		int end = 2;

		for (int i=0; i<10; i++) {
			end++;
		}

		Event e = new Event(0, end);

		e.switchLights(4);
	}
}