package soot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.rse.utils.Configuration;
import ch.ethz.rse.verify.ClassToVerify;
import soot.options.Options;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

/**
 * Helper for Soot-related tasks
 *
 */
public class SootHelper {

	private static final Logger logger = LoggerFactory.getLogger(SootHelper.class);

	public static SootClass loadClassAndAnalyze(ClassToVerify c) {
		SootClass sc = SootHelper.loadClass(c);
		SootHelper.runPointsToAnalysis();
		return sc;
	}

	/**
	 * Load the referenced class and all related classes
	 * 
	 * @param c the class to test
	 * @return the Soot representation of c
	 */
	public static SootClass loadClass(ClassToVerify c) {
		long startTime = System.nanoTime();

		SootClass sc = SootHelper.loadClassInternal(c);

		long endTime = System.nanoTime();
		long durationMilliseconds = (endTime - startTime) / 1000000;
		logger.debug("Runtime: Loaded {} after {}ms", sc.name, durationMilliseconds);

		return sc;
	}

	private static SootClass loadClassInternal(ClassToVerify c) {
		// sometimes, the wrong java version leads to weird behavior (bugs or
		// performance issues)
		logger.info("Using Java version {}", System.getProperty("java.version"));

		String classesDir = c.getClassPath().toString();
		String classToAnalyze = c.getPackageName();

		// reset previously loaded classes (important for consecutive analysis)
		logger.info("Resetting Soot.");
		G.reset();

		// Helpful resources for creating this code:
		// https://github.com/Sable/heros/wiki/Example:-Using-Heros-with-Soot
		// https://stackoverflow.com/questions/48620178/how-can-i-set-up-soot-when-using-it-as-a-library
		// https://o2lab.github.io/710/p/a1.html

		// construct classpath to use when loading the examples
		String javaHome = Configuration.props.getSootJavaHome();
		if (javaHome == null) {
			logger.error("JAVA_HOME not set: " + javaHome);
		}
		String rt = javaHome + "/jre/lib/rt.jar";
		String jce = javaHome + "/jre/lib/jce.jar";
		String classpath = classesDir + ":" + rt + ":" + jce;
		// set classpath
		logger.debug("Soot classpath:" + classpath);
		Scene.v().setSootClassPath(classpath);

		// Enable whole-program mode
		Options.v().set_whole_program(true);
		Options.v().set_app(true);

		// allow phantom references (needed to enable pointer analysis)
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_allow_phantom_elms(true);

		// exclude Java library
		Options.v().set_no_bodies_for_excluded(true);

		// produce more detailed output (helpful for debugging purposes)
		Options.v().set_verbose(true);

		// load the class
		logger.info("Loading {} into Soot", classToAnalyze);
		SootClass sc = Scene.v().loadClass(classToAnalyze, SootClass.BODIES);
		sc.setApplicationClass();

		Scene.v().loadNecessaryClasses();

		Scene.v().setEntryPoints(sc.getMethods());

		for (SootMethod method : sc.getMethods()) {
			method.retrieveActiveBody();
			logger.debug("Loaded method {} with body:\n{}", method.toString(), method.getActiveBody());
		}
		for (SootField field : sc.getFields()) {
			logger.debug("Loaded field {}", field.toString());
		}

		return sc;
	}

	public static void runPointsToAnalysis() {
		long startTime = System.nanoTime();

		SootHelper.runPointsToAnalysisInternal();

		long endTime = System.nanoTime();
		long durationMilliseconds = (endTime - startTime) / 1000000;
		logger.debug("Runtime: Ran points-to analysis in {}ms", durationMilliseconds);
	}

	private static void runPointsToAnalysisInternal() {
		// Enable SPARK call-graph construction
		// Documentation of options:
		// https://soot-build.cs.uni-paderborn.de/public/origin/master/soot/soot-master/3.0.0/options/soot_options.htm#phase_5_2
		Options.v().setPhaseOption("cg", "on");
		Options.v().setPhaseOption("cg.spark", "on");
		Options.v().setPhaseOption("cg.spark", "enabled:true");
		Options.v().setPhaseOption("cg.spark", "verbose:true");
		Options.v().setPhaseOption("cg.spark", "on-fly-cg:true");
		// only consider application classes when building the callgraph. The
		// resulting callgraph will be inherently unsound. Still, this option
		// can make sense for efficiency reasons
		Options.v().setPhaseOption("cg.spark", "apponly:true");

		// SPARK requires jimple format
		// Helpful source: https://github.com/Sable/soot/issues/332
		Options.v().set_output_format(Options.output_format_jimple);

		// run SPARK call-graph construction
		logger.info("Running call-graph construction");
		PackManager.v().runPacks();
		logger.info("Finished call-graph construction");
	}

	public static boolean isIntValue(Value val) {
		// sometimes, Soot represents integers as short or byte
		// For example: "int i = 10"
		return val.getType().toString().equals("int") || val.getType().toString().equals("short")
				|| val.getType().toString().equals("byte");
	}

	public final static UnitGraph getUnitGraph(SootMethod method) {
		Body b = method.retrieveActiveBody();
		logger.debug("Analysing:\n" + b);
		UnitGraph g = new BriefUnitGraph(b);
		return g;
	}
}
