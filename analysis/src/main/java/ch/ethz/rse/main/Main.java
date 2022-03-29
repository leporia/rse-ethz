package ch.ethz.rse.main;

import ch.ethz.rse.VerificationProperty;
import ch.ethz.rse.VerificationResult;
import ch.ethz.rse.VerificationTask;
import org.apache.commons.cli.*;

// DO NOT MODIFY THIS FILE

/**
 * Entry point for verifying a given program
 */
public class Main {
	
	public static void main(String[] args) throws ParseException {
		// prepare parser
		Options options = new Options();
		// parse package name
        Option packageNameOption = new Option("n", "packageName", true, "Fully qualified name of class to check");
        packageNameOption.setRequired(true);
        options.addOption(packageNameOption);
		// parse property to verify
        Option propertyOption = new Option("p", "property", true, "Property to check");
        propertyOption.setRequired(true);
		options.addOption(propertyOption);
		// build parser
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);
		
		// prepare task
		String packageName = cmd.getOptionValue("n");
		String property = cmd.getOptionValue("p");
		VerificationTask t = new VerificationTask(packageName, VerificationProperty.valueOf(property));

		// run verification
		VerificationResult result = Runner.verify(t);
		System.out.flush();
		System.err.flush();
		System.out.println("FINAL OUTPUT:" + result.toString());
	}

}