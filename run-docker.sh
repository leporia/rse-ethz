#!/bin/bash

# ==================================================================
# DO NOT MODIFY THIS FILE! (we will overwrite this file for grading)
# ==================================================================
#
# Run a command in the docker image. If no command is provided, runs the image
# interactively, as a shell
#
# Run as: ./run-docker.sh COMMAND
#
# Note: all commands involving docker use sudo if needed

# navigate to the directory containing this script
cd "$(dirname "$0")"

# do not enter docker if we are already in the right environment
RSE=${RSE:-}
if [[ ! -z "${RSE}" ]]; then
	cd analysis
	$@
	exit $?
fi

# enable bash strict mode
# http://redsymbol.net/articles/unofficial-bash-strict-mode/
set -euo pipefail

# prepare variables
IMAGE=ethsrilab/rse-project:1.3
PROJECTROOT="$(pwd -P)"

# checking if running docker needs sudo
my_docker() {
	# on ubuntu, sudo is typically required to use docker commands
	# on mac os, sudo is typically not required
	if [[ $(docker version 2>&1 | grep -e "Cannot connect" -e "permission") ]]; then
		# need sudo
		sudo docker "$@"
		# change ownership of files owned by root 
		find . -group root -cmin -5 | xargs --no-run-if-empty sudo chown -R $USER:$USER
	else
		# does not need sudo
		docker "$@"
	fi
}

# run docker
# --rm: removes the container after exiting
# --net none: disable network
# -v: mount the project root under /project
# --workdir: move to /project
if [ $# -eq 0 ]; then
	# interactive mode
	my_docker run \
		--rm \
		--net none \
		-it \
		-v "$PROJECTROOT":/project \
		--workdir="/project/analysis" \
		$IMAGE
else # run command
	my_docker run \
		--rm \
		--net none \
		-v "$PROJECTROOT":/project \
		--workdir="/project/analysis" \
		$IMAGE \
		bash -c "$@"
fi
