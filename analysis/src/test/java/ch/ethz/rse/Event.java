package ch.ethz.rse;

/**
 * We are verifying calls into this class
 * 
 */
public final class Event {

	// start and end time of this event
	private final int start, end;

	public Event(int start, int end) {
		// check START_END_ORDER
		assert start <= end;
		this.start = start;
		this.end = end;
	}

	public void switchLights(int time) {
		// check AFTER_START
		assert time >= this.start;
		// check BEFORE_END
		assert time <= this.end;
	}
}
