# Specification for docker image
#
# Run and deploy by following the instructions in the Makefile

FROM mcr.microsoft.com/vscode/devcontainers/base:ubuntu-20.04 as rse
# https://hub.docker.com/_/microsoft-vscode-devcontainers
#
# Development container images for use with VS Code Remote - Containers and
# Codespaces. Based on Ubuntu 20.04

#######################
# BASIC PREREQUISITES #
#######################

# tagging the docker image by an environment variable
ENV RSE TRUE

# DEBIAN_FRONTEND=noninteractive: use default settings where possible
RUN apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y \
		build-essential \
		wget \
		git \
		tar \
		vim \
		sed \
	&& rm -rf /var/lib/apt/lists/*

########
# JAVA #
########

# Java 8 is needed for Soot and Apron
# Java 13 is needed for vscode tool support
RUN apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y \
	openjdk-8-jdk \
	openjdk-13-jdk \
	maven \
	&& rm -rf /var/lib/apt/lists/*

# set environment variables (needed for apron installation)
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64

#########
# APRON #
#########

# install basic APRON prerequisites (following https://github.com/antoinemine/apron)
RUN apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y \
		gcc \
		libgmp-dev \
		libmpfr-dev \
		perl \
		sed \
		m4 \
	&& rm -rf /var/lib/apt/lists/*

ENV APRON /opt/apron

# install APRON: https://github.com/antoinemine/apron
# - specific commit label
# - sed: bugfix in ./configure (handle special characters in javadoc)
# - JAVA_HOME: needed in case of sudo
RUN mkdir -p $APRON && \
		git clone https://github.com/antoinemine/apron.git $APRON && \
		cd $APRON && \
		git checkout v0.9.12 && \
		sed -i -e "s#JAVADOC   = .*#JAVADOC   = \$javadoc -encoding UTF-8#g" ./configure && \
		JAVA_HOME=$JAVA_HOME ./configure -no-ppl && \
		make && \
		make install

# debugging information (enable this to investigate errors)
# RUN cat $APRON/configure
# RUN cat $APRON/Makefile.config
# RUN echo $JAVA_HOME
# RUN ls -l $JAVA_HOME/include
# RUN ls -l $JAVA_HOME/include/linux

# potential fixes for errors:
# sed -i -e "s#checkinc \"\$cc \$cflags -I\$JAVA_HOME/include -I\$JAVA_HOME/include/linux\"#checkinc \"\$cc \$cflags -I\$JAVA_HOME/include -I\$JAVA_HOME/include/linux -I\$JAVA_HOME/include/darwin\"#g" ./configure
# sed -i -e "s#JNIINC    =.*#JNIINC    = -I$JAVA_HOME/include -I$JAVA_HOME/include/linux -I$JAVA_HOME/include/darwin#g" Makefile.config

# in case of setup issues on MacOS, these steps may help
# (https://stackoverflow.com/questions/3621158/jni-unsatisfiedlinkerror-loadlibrary-always-fails):
# RUN mv /usr/local/lib/libjapron.so /usr/local/lib/libjapron.dylib RUN mv
# /usr/local/lib/libjgmp.so /usr/local/lib/libjgmp.dylib
# RUN update_dyld_shared_cache

# generate apron documentation
RUN cd $APRON/japron && \
	make doc && \
	jar cvf japron-sources.jar . .

##############
# MAVEN JARS #
##############

# add generated jars to maven
# - WARNING: do not use sudo when you run this on your host
RUN mvn install:install-file \
	-Dfile=$APRON/japron/gmp.jar \
	-DgroupId=gmp \
	-DartifactId=gmp \
	-Dversion=0.1 \
	-Dpackaging=jar \
	-DgeneratePom=true \
	-Durl=file://$APRON/japron/gmp \
	-Dsources=$APRON/japron/japron-sources.jar

# WARNING: do not use sudo when you run this on your host
RUN mvn install:install-file \
	-Dfile=$APRON/japron/apron.jar \
	-DgroupId=apron \
	-DartifactId=apron \
	-Dversion=0.1 \
	-Dpackaging=jar \
	-DgeneratePom=true \
	-Durl=file://$APRON/japron/apron \
	-Dsources=$APRON/japron/japron-sources.jar

######################
# MAVEN DEPENDENCIES #
######################
# Run every relevant commands once. This downloads all dependencies, which
# avoids re-downloading them on every use of this image.
#
# NOTE: Skip all commands after this note if you are installing locally.

# run the commands in separate stage to avoid including the code in the final
# image
FROM rse as maven-dependencies

ENV MAVEN /tmp/analysis
RUN mkdir -p $MAVEN
ADD analysis-tmp $MAVEN
RUN cd $MAVEN && \
	mvn -q \
		clean \
		test \
		verify \
		site \
		surefire-report:report \
		compile \
		exec:java -Dexec.mainClass="ch.ethz.rse.testing.VerificationTestCaseCollector" \
		assembly:single \
		|| true

# switch back to the main image
FROM rse as final

# copy dependencies from separate stage
COPY --from=maven-dependencies /root/.m2 /root/.m2

# check that analysis directory is not included in the final image
RUN (! ls /tmp/analysis 2>/dev/null) || exit 1
