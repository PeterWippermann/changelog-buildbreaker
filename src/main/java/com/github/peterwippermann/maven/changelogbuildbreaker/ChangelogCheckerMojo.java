package com.github.peterwippermann.maven.changelogbuildbreaker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
 * Typically to be used in the preparation phase of a released to ensure that
 * the changelog is in-sync with the release.
 */
@Mojo(name = "check")
public class ChangelogCheckerMojo extends AbstractMojo {
	/**
	 * ## [Unreleased]
	 * 
	 */
	private static final String DEFAULT_PATTERN_FOR_UNRELEASED_TAG = ".*?\\[Unreleased\\]";

	private static final String DEFAULT_ENCODING = "UTF-8";

	/**
	 * Location of the file.
	 */
	@Parameter(defaultValue = "./CHANGELOG.MD", property = "changelogFile")
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
	@Parameter(defaultValue = DEFAULT_PATTERN_FOR_UNRELEASED_TAG, property = "unreleasedTagPattern")
	private String unreleasedTagPattern;

	public void execute() throws MojoExecutionException, MojoFailureException {
		checkAndSetPreconditions();

		getLog().debug("Checked preconditions");
		getLog().info("Checking changelog for pattern: " + unreleasedTagPattern);
		Pattern pattern = Pattern.compile(unreleasedTagPattern);

		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(changelogFile), encoding))) {
			int x = 0;
			for (String line; (line = br.readLine()) != null;) {
				x++;
				if (pattern.matcher(line).matches()) {
					getLog().error("Line #" + x + " \"" + line + "\" matches the pattern \"" + pattern.pattern()
							+ "\"! Aborting the build...");
					throw new MojoFailureException(
							"The changelog file still contains unreleased changes. Did you forget to update the changelog?");
				} else {
					// TODO Only log this in a "very verbose mode"
					getLog().debug("Line #" + x + " \"" + line + "\" doesn't match.");
				}
			}
		} catch (IOException e) {
			throw new MojoExecutionException("Exception while reading file", e);
		}
		getLog().info("Changelog does not contain unreleased changes");

	}

	private void checkAndSetPreconditions() throws MojoExecutionException {
		if (encoding == null) {
			getLog().warn("The expected file encoding of the changelog file has not been set! Defaulting to "
					+ DEFAULT_ENCODING + ".");
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
					"The changelog file " + changelogFile.getAbsolutePath() + " does not exist!");
		} else {
			getLog().debug("The changelog file exists.");
		}

		if (!changelogFile.canRead()) {
			throw new MojoExecutionException("The changelog file " + changelogFile.getAbsolutePath()
					+ " can't be read! Hint: Check file permissions.");
		}
	}
}