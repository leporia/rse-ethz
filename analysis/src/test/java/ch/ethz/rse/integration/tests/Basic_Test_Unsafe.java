// DISABLED (by removing this line, you can enable this test to check if you are sound)
package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Event;

// expected results:
// START_END_ORDER UNSAFE
// AFTER_START UNSAFE
// BEFORE_END UNSAFE

public class Basic_Test_Unsafe {

	public void m2(int i, int j) {
		Event e = new Event(2, 0);
		e.switchLights(1);
	}
}