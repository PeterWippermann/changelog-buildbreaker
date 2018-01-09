Changelog Buildbreaker Maven Plugin
===
[![Build Status](https://travis-ci.org/PeterWippermann/changelog-buildbreaker.svg?branch=master)](https://travis-ci.org/PeterWippermann/changelog-buildbreaker)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.peterwippermann.maven/changelog-buildbreaker-maven-plugin.svg)](https://mvnrepository.com/artifact/com.github.peterwippermann.maven/changelog-buildbreaker-maven-plugin)

_A Maven plugin that checks your changelog for changes that aren't tied to a version number._

## Contents

- [Reasoning](#reasoning)
- [Check and remedy for unreleased changes](#check-and-remedy-for-unreleased-changes)
- [Integration and usage](#integration-and-usage)
- [Examples](#examples)
- [Configuration options](#configuration-options)
- [Understanding the RegEx](#understanding-the-regex)


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
```sh
mvn com.github.peterwippermann.maven:changelog-buildbreaker-maven-plugin:check
```
Maven will download the plugin automatically and run its `check` goal.
However, even when not binding the plugin to a certain Maven phase, it's a good practice to explicitly pin the plugin's version by declaring it in your build configuration (see below).

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
This check __will also affect your local `mvn install`!__ So if you want to have your changelog checked only for a release, you can move this plugin execution to a dedicated [Maven profile](http://maven.apache.org/guides/introduction/introduction-to-profiles.html) like "release".

### Integrate the check with the Maven Release Plugin

If you are using the [Maven Release Plugin](http://maven.apache.org/maven-release/maven-release-plugin/) for releasing, you can easily have it execute the Changelog Buildbreaker Plugin in the preparation of the release.

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
Now, when preparing a release with `mvn release:prepare` the changelog will also be checked.

## Examples
In `src/test/resources` you find some examples of valid and invalid `CHANGELOG` files. The general rule is: The plugin
raises an error if there is a section `## [Unreleased]` that is NOT followed by another line starting with `##`,
regardless of blank lines in between:

### Passing examples
```markdown
## [Unreleased]
 
```

```markdown
## [Unreleased]
 
## Another section
 
```

### Failing examples
```markdown
## [Unreleased]
- Fixed that mean bug
```

```markdown
## [Unreleased]
### Added
- A cool feature
```

## Configuration options
The following snippet illustrates the configuration parameters when referencing the plugin in your POM.
The configuration __values in the example are the defaults__. So if you stick to the convention you don't have to set them.
```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.github.peterwippermann.maven</groupId>
      <artifactId>changelog-buildbreaker-maven-plugin</artifactId>
      [...]
      <configuration>
        <changelogFile>CHANGELOG.MD</changelogFile>
        <encoding>UTF-8</encoding>
        <unreleasedChangesPattern>(?:^|\\R)(?&lt;section&gt;##\\h*\\[Unreleased\\]\\h*)\\R(?:\\h*\\R)*(?&lt;content&gt;\\h*(?!##\\h*\\[)\\p{Graph}+.*)(?:$|\\R)</unreleasedChangesPattern>
      </configuration>
    </plugin>
  </plugins>
</build>
```

## Understanding the RegEx
Although the plugin only checks for an empty _Unreleased_ section, the Regular Expression used is far from trivial:
```regex
(?:^|\\R)(?<section>##\\h*\\[Unreleased\\]\\h*)\\R(?:\\h*\\R)*(?<content>\\h*(?!##\\h*\\[)\\p{Graph}+.*)(?:$|\\R)
```

If you want to use your own, modified RegEx, here's what you need to know about the original version:
* `\\` - In RegEx special characters are masked by a backslash `\`. However, in XML a backslash has to be escaped as well. Thus a double backslash `\\` has to be used in the XML configuration but not in the plugin's Java sources. 
* `(?:^|\\R)` - The _unreleased_ section's heading is preceded by a line break or even by the beginning of the file
* `(?<section>##\\h*\\[Unreleased\\]\\h*)\\R` - Locates the actual section heading _"[Unreleased]"_. An arbitrary number of (horizontal) whitespaces are allowed at the beginning and the end. The match is assigned to a named group _"section"_ and will be printed in the logs during plugin execution.
* `(?:\\h*\\R)*` - An arbitrary number of "empty lines", which may also include whitespaces.
* `(?<content>\\h*(?!##\\h*\\[)\\p{Graph}+.*)` - A named group _"content"_, which matches any printable characters - except for a 2nd-order heading. That 2nd-order heading would be the latest release.
* `(?:$|\\R)` - The unreleased content is followed by a line break or end of file (EOF). 
* The two named groups _section_ and _content_ are optional. But if they one of them is defined, in case of a match its content will be logged.
 