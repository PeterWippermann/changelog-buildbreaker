package com.github.peterwippermann.maven.changelogbuildbreaker;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Says "Hi" to the user.
 *
 */
@Mojo(name = "sayhi")
public class GreetingMojo extends AbstractMojo {
	/**
	 * Location of the file.
	 */
	@Parameter(defaultValue = "./CHANGELOG.MD", property = "changelogFile")
	private File changelogFile;

	public void execute() throws MojoExecutionException {
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

		getLog().info("Hello, world.");
	}
}