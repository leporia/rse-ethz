#!/bin/bash

# =================================================================
# DO NOT MODIFY THIS FILE (we will overwrite this file for grading)
# =================================================================
#
#
# Run as: ./run.sh ch.ethz.rse.integration.tests.Basic_Test_Safe START_END_ORDER

# navigate to the directory containing this script
cd "$(dirname "$0")"

export LD_LIBRARY_PATH=/usr/local/lib
export JAVA=$JAVA_HOME/bin/java

JAR=./target/analysis-0.0.1-jar-with-dependencies.jar

# compile if necessary
if [ ! -f $JAR ]; then
    mvn -Dmaven.test.skip=true --batch-mode compile assembly:single
fi

# enforce runtime and memory limit
time timeout 10 $JAVA -enableassertions -Xmx1G -jar $JAR -n $1 -p $2

# record exit status
RETVAL=$?

if [ $RETVAL -eq 124 ]; then
	# timeout
	echo "" # ensure new line before
	echo "FINAL OUTPUT:ERROR.Timeout"
	echo "" # ensure new line after
elif [ $RETVAL -ne 0 ]; then
	echo "" # ensure new line before
	echo "FINAL OUTPUT:ERROR.Crash"
	echo "" # ensure new line after
fi

# propagate exit status
exit $RETVAL