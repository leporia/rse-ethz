package ch.ethz.rse.integration.invalid_input_tests;

import ch.ethz.rse.Event;

public class Basic_Test_Global_Array {
	Event e[] = new Event[1];

	public void m1() {
		this.e[0] = new Event(2, 4);
		this.e[0].switchLights(3);
	}
}