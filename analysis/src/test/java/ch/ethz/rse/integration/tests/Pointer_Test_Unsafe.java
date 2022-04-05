package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER SAFE
// AFTER_START SAFE
// BEFORE_END UNSAFE

public class Pointer_Test_Unsafe {
	public static void m1() {
		Event e = new Event(1, 2);
		Event foo = new Event(1, 5);
		foo = e;
		foo.switchLights(3);
	}
}