package com.github.peterwippermann.maven.changelogbuildbreaker;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Checks the project's changelog file and breaks the build, if the changelog
 * contains changes tagged as "unreleased".
 * <p>
 * Typically to be used in the preparation phase of a release to ensure the
 * changelog is in-sync with the release.
 */
@Mojo(name = "check")
public class ChangelogCheckerMojo extends AbstractMojo {

	/**
	 * Will match the following content
	 * 
	 * ## [Unreleased]
	 * 
	 * ### Added - new features
	 * 
	 * ## [2.0.2] - 2017-11-23
	 */
	static final String DEFAULT_PATTERN_FOR_UNRELEASED_CHANGES = "\\R(?<section>##\\h*\\[Unreleased\\]\\h*)\\R(?:\\h*\\R)*(?<content>\\h*(?!##\\h*\\[)\\p{Graph}+.*)\\R";

	private static final String UNRELEASED_CONTENT_CAPTURING_GROUP_NAME = "content";

	private static final String UNRELEASED_SECTION_CAPTURING_GROUP_NAME = "section";

	private static final String DEFAULT_ENCODING = "UTF-8";
	
	private static final String DEFAULT_FILE = "./CHANGELOG.MD";

	/**
	 * Location of the file.
	 */
	@Parameter(defaultValue = DEFAULT_FILE, property = "changelogFile")
	private File changelogFile;

	/**
	 * Encoding of the file.
	 * <p>
	 * The general build property project.build.sourceEncoding is used as a
	 * fallback. Will be defaulted to UTF-8 otherwise.
	 */
	@Parameter(defaultValue = "${project.build.sourceEncoding}", property = "encoding")
	private String encoding;

	/**
	 * RegEx-Pattern for the "Unreleased" section
	 */
	@Parameter(defaultValue = DEFAULT_PATTERN_FOR_UNRELEASED_CHANGES, property = "unreleasedChangesPattern")
	private String unreleasedChangesPattern;

	public void execute() throws MojoExecutionException, MojoFailureException {
		checkAndSetPreconditions();

		String changelogFileContent;
		try {
			changelogFileContent = new String(Files.readAllBytes(changelogFile.toPath()), Charset.forName(encoding));
		} catch (IOException e) {
			throw new MojoExecutionException("Could not read the changelog file!", e);
		}

		getLog().info("Checking changelog for pattern: " + unreleasedChangesPattern);
		Pattern pattern = Pattern.compile(unreleasedChangesPattern);

		Matcher matcher = pattern.matcher(changelogFileContent);
		if (matcher.find()) {
			getLog().error("Found unreleased changes in the changelog file! Aborting the build...");
			try {
				getLog().info("Unreleased section: \"" + matcher.group(UNRELEASED_SECTION_CAPTURING_GROUP_NAME) + "\"");
			} catch (IllegalArgumentException e) {
				getLog().debug("No capturing group named \"" + UNRELEASED_SECTION_CAPTURING_GROUP_NAME
						+ "\" has been defined in the RegEx pattern! Thus the match for the \"Unreleased section\" can't be logged.");
			}

			try {
				getLog().info("Unreleased content: \"" + matcher.group(UNRELEASED_CONTENT_CAPTURING_GROUP_NAME) + "\"");
			} catch (IllegalArgumentException e) {
				getLog().debug("No capturing group named \"" + UNRELEASED_CONTENT_CAPTURING_GROUP_NAME
						+ "\" has been defined in the RegEx pattern! Thus the match for the \"Unreleased content\" can't be logged.");
			}

			throw new MojoFailureException(
					"The changelog file still contains unreleased changes. Did you forget to update the changelog?");
		} else {
			getLog().info("Did not find any unreleased changes.");
		}
	}

	private void checkAndSetPreconditions() throws MojoExecutionException {
		if (encoding == null) {
			getLog().warn("The expected file encoding of the changelog file has not been set! Defaulting to "
					+ DEFAULT_ENCODING
					+ ". To remove this warning either set <project.build.sourceEncoding> in your Maven project or set the \"encoding\" configuration parameter of this plugin.");
			encoding = DEFAULT_ENCODING;
		}
		getLog().debug("The expected encoding of the changelog file is: " + encoding);

		if (changelogFile == null) {
			throw new MojoExecutionException("The path to the changelog file has not been set!");
		} else {
			getLog().debug("The path of the changelog file is: " + changelogFile.getPath());
		}

		if (!changelogFile.exists()) {
			throw new MojoExecutionException(
					"The changelog file " + changelogFile.getAbsolutePath() + " does not exist! Use the \"changelogFile\" configuration parameter of this plugin to set a custom file path.");
		} else {
			getLog().debug("The changelog file exists.");
		}

		if (!changelogFile.canRead()) {
			throw new MojoExecutionException("The changelog file " + changelogFile.getAbsolutePath()
					+ " can't be read! Did you set proper file permissions?");
		}
	}

	void setChangelogFile(File changelogFile) {
		this.changelogFile = changelogFile;
	}

	void setUnreleasedChangesPattern(String unreleasedChangesPattern) {
		this.unreleasedChangesPattern = unreleasedChangesPattern;
	}
}