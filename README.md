# RSE Project Development

In this file, we explain how to set up, run, and develop this project. Before
reading this, you should read the [project
description](/resources/project-description/project.md).

## Pipeline Status

In the
[online](https://gitlab.inf.ethz.ch/COURSE-RSE-2022/yolo#pipeline-status)
version of this file, you can see the current status of the project.

Current pipeline status: ![pipeline
status](https://gitlab.inf.ethz.ch/COURSE-RSE-2022/yolo/badges/master/pipeline.svg)

Current code coverage: ![code coverage](https://gitlab.inf.ethz.ch/COURSE-RSE-2022/yolo/badges/master/coverage.svg)

## Frameworks

The skeleton leverages various frameworks that simplify and aid software
development. In the following, we provide a quick introduction, with a focus on
how these frameworks help you complete this project.

We provide instructions exclusively for Linux (in particular, for Ubuntu). We
strongly recommend you use Ubuntu to develop and run this project.

### Docker

The project is set up to use [docker](https://www.docker.com/) - if you are
curious, you can find a simple introduction
[here](https://docker-curriculum.com/). Docker essentially simulates a
lightweight virtual machine, which allows us to work in a controlled environment
that comes with the necessary dependencies for this project. To install docker,
you may follow [these](https://docs.docker.com/engine/install/ubuntu/)
instructions.

We have prepared a docker image containing all tools needed to run and develop
this project. To run this docker image in interactive mode (i.e., in a terminal
where you can run any command), run

```bash
./run-docker.sh
[...]
root@a515c5af06d6:/project/analysis$ TYPE YOUR COMMAND HERE...
```

In the following, we assume you are inside the docker image, either because you
ran the previous command, or because you are developing inside the container
(discussed later).

Alternatively, you could manually follow the instructions inside the
[Dockerfile](docker/Dockerfile) to install all relevant tools directly on your
system. However, this should not be necessary, and we recommend not to do this.

### Maven

We manage the project's build, reporting and testing using
[Maven](https://maven.apache.org/). The most important maven commands are:

```bash
# delete automatically generated files
root@a515c5af06d6:/project/analysis$ mvn clean
[...]
# compile, run unit and integration tests, report code coverage
root@a515c5af06d6:/project/analysis$ mvn verify
[...]
# generate test report
root@a515c5af06d6:/project/analysis$ mvn site
[...]
```

Command `mvn verify` generates information on test coverage
[here](/analysis/target/site/jacoco-merged-test-coverage-report/index.html).
Command `mvn site` generates a report on test results
[here](/analysis/target/site/index.html).

#### Common issues

- If you get unexpected errors that do not make sense, it often helps to run
  `mvn clean` and try again.

### Unit and Integration Testing

We have set up maven to run unit tests (detected by filename pattern
`*Test.java`) and integration tests (detected by filename pattern `*IT.java`),
which are located in [this directory](/analysis/src/test/java).

The most important maven commands regarding testing are

```bash
# run unit tests and create "surefire" report
root@a515c5af06d6:/project/analysis$ mvn test surefire-report:report
# run integration tests and create "failsafe" report
root@a515c5af06d6:/project/analysis$ mvn verify -Dskip.surefire.tests site -Dskip.surefire.tests
```

The two reports are located in
[surefire-report.html](analysis/target/site/surefire-report.html) and
[failsafe-report.html](analysis/target/site/failsafe-report.html), respectively.

### Logging

We use [SLF4J](http://www.slf4j.org/) as a front-end for logging, and
[Logback](http://logback.qos.ch/) as our logging backend.

While logging is not necessary to solve this project, it is easy to do and can
be very helpful. See
[Runner.java](analysis/src/main/java/ch/ethz/rse/main/Runner.java) for an simple
usage example of logging in action.

Feel free to adapt the [logging
configuration](./analysis/src/main/resources/logback.xml) if needed. For
example, you can reduce the console logging information by adapting the "Log
level for console".

### GitLab CI/CD

We have set up the project to build and test the project on every push to the
code repository, as controlled by [.gitlab-ci.yml](.gitlab-ci.yml) and
[.gitlab-ci-custom.yml](.gitlab-ci-custom.yml). When the tests fail, you will be
notified by e-mail (depending on your GitLab [notification
settings](https://gitlab.inf.ethz.ch/-/profile/notifications)).

We recommend that you start to develop new functionality by writing
corresponding tests, i.e., that you adhere to [test-driven
development](https://en.wikipedia.org/wiki/Test-driven_development).

#### Common issues

- `ERROR: Job failed (system failure)`: Likely, running the job again will
  resolve this issue. To this end, in GitLab, navigate to CI/CD -> Jobs -> Click
  on the box "Failed" of the failed job -> Click "Retry" (top right)

### JaCoCo

We use [JaCoCo](https://www.eclemma.org/jacoco/) to record and report the code
coverage achieved by all tests. When running `mvn verify`, the code coverage is
reported
[here](/analysis/target/site/jacoco-merged-test-coverage-report/index.html). As
discussed in the project description, we will award additional points for a
instruction coverage of `>=75%`.

## Sanity Check for Submission

<span style="color:red">**IMPORTANT NOTE:**</span> To ensure we will be able to run your submission, follow these rules:

1. Some files in this repository come with a note to `NOT MODIFY THIS FILE`. We
   will overwrite these files for grading, so changing them may mean that we
   cannot compile your project.
2. Before submission, check that the [GitLab CI/CD](#gitlab-cicd) runs without
   errors by checking the pipeline status (see above). In particular, please
   make sure that you do not commit failing tests as those could lead to errors in test coverage calculation. We will use the same workflow
   as the GitLab CI/CD to test your submission.

## Development (Optional but Recommended)

To develop, debug, and run this project, we suggest using [Visual Studio
Code](https://code.visualstudio.com/) to [develop inside a docker
container](https://code.visualstudio.com/docs/remote/containers). This allows
you to work on this project without having to install its dependencies on your
host system.

### Installation

To this end, follow [these installation
instructions](https://code.visualstudio.com/docs/remote/containers#_installation).
Instead of "adding your user to the docker group" (which opens up a potential
[security hole](https://docs.docker.com/engine/install/linux-postinstall/)), you
may instead install docker in [rootless
mode](https://docs.docker.com/engine/security/rootless/#install).

Then, install the [Java Extension
Pack](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)
in Visual Studio code:

- Open Visual Studio Code
- Launch VS Code Quick Open (Ctrl+P)
- Paste the following command, and press enter: `ext install
  vscjava.vscode-java-pack`
- Launch VS Code Quick Open again (Ctrl+P)
- Paste the following command, and press enter: `ext install ms-vscode-remote.remote-containers`

### Usage

To open the project in Visual Studio Code:

- Open Visual Studio Code
- Press F1, type "Remote-containers: Open folder in Container"
- Navigate to directory [analysis](analysis), and "Open" it
- Wait a few minutes for everything to load

To run all unit tests:

- Click on tab "Test" in the left-hand-side panel
- Click "Run All Tests" in the new panel (or "Debug All Tests" to debug)
- Wait for the test results to pop up
