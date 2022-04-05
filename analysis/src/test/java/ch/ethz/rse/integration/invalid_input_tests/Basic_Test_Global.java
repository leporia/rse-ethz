package ch.ethz.rse.integration.invalid_input_tests;

import ch.ethz.rse.Event;

public class Basic_Test_Global {
	Event e;

	public void m1() {
		this.e = new Event(2, 4);
		this.e.switchLights(3);
	}
}