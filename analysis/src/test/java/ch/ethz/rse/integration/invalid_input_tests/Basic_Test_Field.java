package ch.ethz.rse.integration.invalid_input_tests;

import ch.ethz.rse.Event;

class EventWrapper {
	public Event e;
}

public class Basic_Test_Field {
	public void m1() {
		EventWrapper ew = new EventWrapper();
		ew.e = new Event(2, 5);
		ew.e.switchLights(3);
	}
}