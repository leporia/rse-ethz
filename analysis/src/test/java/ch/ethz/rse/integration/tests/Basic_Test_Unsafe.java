package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER UNSAFE
// AFTER_START UNSAFE
// BEFORE_END UNSAFE

public class Basic_Test_Unsafe {
	// note that in theory the following code should be in a separate class
	public void m2(int i, int j) {
		Event e = new Event(2, 0);
		e.switchLights(1);
	}
}