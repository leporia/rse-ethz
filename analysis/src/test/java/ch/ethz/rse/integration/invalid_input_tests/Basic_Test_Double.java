package ch.ethz.rse.integration.invalid_input_tests;

import ch.ethz.rse.Event;

public class Basic_Test_Double {
	public void m1() {
		double k = 5;
		Event e = new Event(2, (int) k);
		e.switchLights(3);
	}
}