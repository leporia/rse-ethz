package ch.ethz.rse.pointer;

import soot.jimple.internal.JInvokeStmt;

/**
 * 
 * Contains information about the initializer of a Event object
 *
 */
public class EventInitializer {

	/**
	 * statement that performs the initialization
	 */
	private final JInvokeStmt statement;

	/**
	 * Unique identifier of the initializer
	 */
	private final int uniqueNumber;

	/**
	 * first argument in the constructor
	 */
	public final int start;

	/**
	 * 
	 * @param statement    piece of code running the initializer
	 * @param uniqueNumber unique identifier of the initializer
	 * @param argment      argument in the constructor
	 */
	public EventInitializer(JInvokeStmt statement, int uniqueNumber, int start) {
		this.statement = statement;
		this.uniqueNumber = uniqueNumber;
		this.start = start;
	}

	/**
	 * 
	 * @return piece of code running the initializer
	 */
	public JInvokeStmt getStatement() {
		return statement;
	}

	/**
	 * 
	 * @return unique identifier of the initializer
	 */
	private int getUniqueNumber() {
		return this.uniqueNumber;
	}

	/**
	 * 
	 * @return unique label of this initializer
	 */
	public String getUniqueLabel() {
		return "AbstractObject" + this.getUniqueNumber() + ".end";
	}

	public String toString() {
		return "AbstractObject" + this.getUniqueNumber();
	}

}