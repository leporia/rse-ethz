SHELL := bash
.ONESHELL:
.SHELLFLAGS := -eu -o pipefail -c

IMAGE := ethsrilab/rse-project:1.3
CONTAINER := rse-project

LOG_DIR := ./image_summary

#########
# IMAGE #
#########

.PHONY: copy-analysis
copy-analysis:
	rsync -a --delete --exclude .devcontainer/ ../analysis/ ./analysis-tmp/

# build the docker image
.PHONY: image
image: clean copy-analysis
	docker build -t $(IMAGE) . | tee $(LOG_DIR)/docker_build_image_output.log

#######
# LOG #
#######

.PHONY: clean
clean:
	rm -rf $(LOG_DIR)
	mkdir $(LOG_DIR)

.PHONY: log
log: clean image
	cp Dockerfile Makefile $(LOG_DIR)
	zip -j $(LOG_DIR)/image_summary.zip $(LOG_DIR)/*

.PHONY: details
details: image
	docker history --no-trunc $(IMAGE)

#######
# RUN #
#######

# launch a docker container using the image, which provides a shell in the
# container
.PHONY: run
run: image
	docker run -it --rm --name $(CONTAINER) $(IMAGE)

##########
# DEPLOY #
##########

# Log in to the docker registry (needed to deploy the image)
.PHONY: login
login:
	docker login

# Deploy the image
.PHONY: deploy
deploy: login image
	docker push $(IMAGE)
