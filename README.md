# changelog-buildbreaker
## Goal
If a file `CHANGELOG.md` is present in the project's root directory, this plugin checks whether the content of the 
`## [Unreleased]` section is empty or not. If there is content available the plugin execution fails, otherwise it 
passes. This can be helpful mainly for Maven's `release` phase: The plugin can check for you if you forgot to insert
the version number above your listed changes.  

## Usage
You can either run this plugin standalone or as part of another Maven phase.

### Standalone
```
mvn changelog-buildbreaker:check
```

### As part of another Maven phase
// TODO

## Examples
In `src/test/resources` you find some examples of (in-)valid `CHANGELOG.md` files. The general rule is: The plugin
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