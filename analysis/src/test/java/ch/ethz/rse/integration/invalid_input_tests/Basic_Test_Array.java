package ch.ethz.rse.integration.invalid_input_tests;

import ch.ethz.rse.Event;

public class Basic_Test_Array {
	public void m1() {
		Event[] e = new Event[1];
		e[0] = new Event(2, 4);
		e[0].switchLights(3);
	}
}