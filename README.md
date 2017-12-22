Changelog Buildbreaker Maven Plugin
===
[![Build Status](https://travis-ci.org/PeterWippermann/changelog-buildbreaker.svg?branch=master)](https://travis-ci.org/PeterWippermann/changelog-buildbreaker)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.peterwippermann.maven/changelog-buildbreaker-maven-plugin.svg)](https://mvnrepository.com/artifact/com.github.peterwippermann.maven/changelog-buildbreaker-maven-plugin)

_A Maven plugin that checks your changelog for changes that aren't tied to a version number._

## Reasoning
__Changelogs should be in sync__ with the corresponding release: It is confusing for your users if the changelog does not reflect the latest changes. An obvious mistake are changes in your changelog labelled as "unreleased".

This Maven plugin checks that there are no more unreleased changes in your changelog and breaks build otherwise. It is __intended to be used in your release build__, since your changelog should have been finalised then.

Your changelog is expected to follow the __format proposed by [keepachangelog.com](http://www.keepachangelog.com)__.

## Check and remedy for unreleased changes 
A `CHANGELOG.md` file has to be present in the project's root directory. An `## [Unreleased]` section has to be present but it must be empty, i.e. no visible characters, but blank lines are allowed. If necessary, the filename and the regular expression used for checking can be configured.

If there are no unreleased changes, the build will silently continue. Otherwise the plugin will make the build fail. All you normally have to do then is to start a new section in your changelog for that current release and move all unreleased changes to that section.

## Integration and usage
You can either run this plugin on demand, bind it to Maven's "verify" phase or integrate it with the Maven Release Plugin.

### Call goal on-demand
You can always run the following without any preparation:
```bash
mvn com.github.peterwippermann.maven:changelog-buildbreaker-maven-plugin:check
```
Maven will download the plugin automatically and run its `check` goal.
However, it's a good practice to explicitly pin the plugin's version by declaring it in your build configuration (see below).

### Bind this plugin to Maven's "verify" phase
```xml
<build>
	<plugins>
		<plugin>
			<groupId>com.github.peterwippermann.maven</groupId>
			<artifactId>changelog-buildbreaker-maven-plugin</artifactId>
			<version>0.1.1</version>
			<executions>
				<execution>
					<id>check-changelog-before-deploy</id>
					<phase>verify</phase>
					<goals>
						<goal>check</goal>
					</goals>
				</execution>
			</executions>
		</plugin>
	</plugins>
</build>
```
Running `mvn deploy` will also include the `verify` phase and thus execute the check.

### Integrate the check with the Maven Release Plugin

If you are using the Maven Release Plugin for releasing, you can easily have it execute the Changelog Buildbreaker Plugin in the preparation of the release.

1. Add the Changelog Buildbreaker Plugin to your build configuration
2. Bind the plugin to the Maven Release Plugin
```xml
<build>
	<plugins>
		<plugin>
			<groupId>com.github.peterwippermann.maven</groupId>
			<artifactId>changelog-buildbreaker-maven-plugin</artifactId>
			<version>0.1.1</version>
		</plugin>
		<plugin>
			<groupId>org.apache.maven.plugins</groupId>
			<artifactId>maven-release-plugin</artifactId>
			[...]
			<configuration>
				<preparationGoals>changelog-buildbreaker:check</preparationGoals>
				<!-- Note that no GroupID is required and the shortname "changelog-buildbreaker" can be used -->
			</configuration>
		</plugin>
	</plugins>
</build>
```
Now, when preparing a release with
```
mvn release:prepare
```
the changelog will also be checked.

## Examples
In `src/test/resources` you find some examples of valid and invalid `CHANGELOG` files. The general rule is: The plugin
raises an error if there is a section `## [Unreleased]` that is NOT followed by another line starting with `##`,
regardless of blank lines in between:

### Passing examples
```
## [Unreleased]
 
```

```
## [Unreleased]
 
## Another section
 
```

### Failing examples
```
## [Unreleased]
### Fixed
```

```
## [Unreleased]
### Added
- A cool feature
```