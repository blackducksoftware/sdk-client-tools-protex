# Black Duck Software Protex SDK Examples

The Protex SDK examples provide code snippets which demonstrate how to manipulate data within a Protex server instance from external client applications

## Building
The SDK examples use [Gradle](https://gradle.org/) for build operations (specifically, the Gradle wrapper, invoked via `gradlew`)

### Importing into an IDE
Instead of using Gradle's plug-ins to generate IDE-specific files, it is assumed that the IDE's Gradle plug-in will be used.

For example, [Eclipse's Gradle Plug-in](https://marketplace.eclipse.org/content/gradle-ide-pack) can be used to import the projects into an Eclipse environment

## Contributions
Contributions are pull request based. Contributors should fork the repository and create pull requests based on the `master` branch

* These libraries are based on Java 6 - all changes should be compatible with this language version
* Changes to APIs must be backwards compatible
* Changes in pull requests should be documented
* If any significant features are added, add tests where possible

## Running Tests

### Requirements 
The tests for the Protex SDK examples require
 
* A Protex server instance to be available and running
* The server URL to be stored in the 'testUrlbase' Gradle property (defined in [gradle.properties](gradle.properties))
* A username to be stored in the 'testUsername' Gradle property (defined in [gradle.properties](gradle.properties))
  * The user must have the Admin, Attorney, Codeprinter, Developer, Manager, Power Developer, and Project Lead roles
  * The user must NOT have the Identification Only or Read Only roles
* The provided user's password to be stored in the 'testPassword' Gradle property. It is recommended to pass this via -PtestPassword during builds, as opposed to storing this value in the [gradle.properties](gradle.properties) file

If you do not have a Protex instance available and must build (not recommended), you may use `-PskipExampleTests` in the Gradle command line to compile the project without running the tests

### Groups of Tests

There are three sets of tests available for the SDK examples. The first set includes only those tests which do not alter the overall state of the server at any time, and do not depend of the availability of source code. The second alters server properties that may affect operations being performed on the server. The last set depends on the presence of source code. All tests attempt to set the server back to its original state after execution.

It is highly recommended to only run tests on non-production systems. If other tasks are being performed on the same system, it is recommended that only the tests which do not affect overall server operation be run.

If you wish to run only non-operation affecting tests, run Gradle with the option `-PskipOperationAffectingTests`

All the tests may be run by executing the TestNG regression suite file located at src/test/resources/all-example-tests.xml

### Configuring Source Code For Tests

The test come with default source which they can upload to the target server for use - however, the source will have to either be left in-place or manually deleted. This upload capability only occurs if the build is run with the default sources allowed option set to true (Gradle command line `-PtestSourcesAllowUpload=true`)

If you do not wish to allow upload, you may specific the path of source files the tests may use (relative to the server root) via setting `-PtestSources=(source location)` in the Gradle command line.

Both of these options may be added to a gradle.properties file for consistent use

## Branches
The repository contains a `master` branch which contains tags for each release and any changes contributed. Releases will be based on what has been merged into the `master` branch 

## Release Pattern
This project does **not** follow semantic versioning - version numbers are kept matching to the minimum compatible product version

New releases of the SDK examples are created when a version of Protex is released. The version number of the client/utility libraries is generally the minimum version of the associated product that release supports.