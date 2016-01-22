# Black Duck Software Protex SDK Libraries

The Black Duck Software Protex SDK Libraries provide access to the SDK end-points available on Black Duck's Protex product

## Building
The SDK libraries use [Gradle](https://gradle.org/) for build operations (specifically, the Gradle wrapper, invoked via `gradlew`)

### Importing into an IDE
Instead of using Gradle's plug-ins to generate IDE-specific files, it is assumed that the IDE's Gradle plug-in will be used.

For example, [Eclipse's Gradle Plug-in](https://marketplace.eclipse.org/content/gradle-ide-pack) can be used to import the projects into an Eclipse environment

## Contributions
Contributions are pull request based. Contributors should fork the repository and create pull requests based on the `master` branch

* These libraries are based on Java 6 - all changes should be compatible with this language version
* Changes to APIs must be backwards compatible
* Changes in pull requests should be documented
* If any significant features are added, add tests where possible

## Branches
The repository contains a `master` branch which contains tags for each release and any changes contributed. Releases will be based on what has been merged into the `master` branch 

## Release Pattern
This project does **not** follow semantic versioning - version numbers are kept matching to the minimum compatible product version

New releases of the SDK libraries are created when a version of Protex is released. The version number of the client/utility libraries is generally the minimum version of the associated product that release supports.

## SLF4j Dependency
The Protex SDK Libraries are intended to be used within other applications. Due to this, at compile and run time, they only depend on the SLF4j APIs, not a specific implementation. This is to allow consuming applications to specify their own implementation without issue. If no implementation is specified, the APIs automatically fall back to a no-op implementation
