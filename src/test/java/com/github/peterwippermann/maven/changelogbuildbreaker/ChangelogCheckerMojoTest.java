package com.github.peterwippermann.maven.changelogbuildbreaker;

import org.apache.maven.plugin.AbstractMojoExecutionException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import static com.github.peterwippermann.maven.changelogbuildbreaker.ChangelogCheckerMojo.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

/**
 * @author jholtkamp
 */
public class ChangelogCheckerMojoTest {

	private ChangelogCheckerMojo sut;

	@Before
	public void setUp() throws Exception {
		sut = new ChangelogCheckerMojo();

		sut.setUnreleasedChangesPattern(DEFAULT_PATTERN_FOR_UNRELEASED_CHANGES);
	}

	@Test
	public void testExecute_fileNotSet() throws Exception {
		sut.setChangelogFile(null);

		assertException(MojoExecutionException.class, "path to the changelog file has not been set");
	}

	@Test
	public void testExecute_missingFile() throws Exception {
		sut.setChangelogFile(new File("this-file-does-not.exist"));

		assertException(MojoExecutionException.class, "does not exist");
	}

	@Test
	public void testExecute_fileCannotBeRead() throws Exception {
		File file = Mockito.mock(File.class);

		when(file.canRead()).thenReturn(false);
		when(file.exists()).thenReturn(true);

		sut.setChangelogFile(file);

		assertException(MojoExecutionException.class, "can't be read");
	}

	@Test
	public void testExecute_nearlyEmptyFile() throws Exception {
		sut.setChangelogFile(getFile("CHANGELOG-nearly-empty.MD"));

		sut.execute();
	}

	@Test
	public void testExecute_emptyUnreleased() throws Exception {
		sut.setChangelogFile(getFile("CHANGELOG-empty-unreleased.MD"));

		sut.execute();
	}

	@Test
	public void testExecute_nonEmptyUnreleased() throws Exception {
		sut.setChangelogFile(getFile("CHANGELOG-non-empty-unreleased.MD"));

		assertForgotToUpdateTheChangelog();
	}

	@Test
	public void testExecute_nonEmptyUnreleased_noStartingBlankLine() throws Exception {
		sut.setChangelogFile(getFile("CHANGELOG-non-empty-unreleased-missing-starting-new-line.MD"));

		assertForgotToUpdateTheChangelog();
	}

	@Test
	public void testExecute_nonEmptyUnreleased_noEndingBlankLine() throws Exception {
		sut.setChangelogFile(getFile("CHANGELOG-non-empty-unreleased-missing-ending-new-line.MD"));

		assertForgotToUpdateTheChangelog();
	}

	@Test
	public void testExecute_keepAChangelogReference_nothingUnreleased() throws Exception {
		sut.setChangelogFile(getFile("CHANGELOG-keep-a-changelog-reference.MD"));

		sut.execute();
	}
	@Test
	public void testExecute_keepAChangelogReference_unreleased() throws Exception {
		sut.setChangelogFile(getFile("CHANGELOG-keep-a-changelog-reference-unreleased.MD"));

		assertForgotToUpdateTheChangelog();
	}

	private void assertForgotToUpdateTheChangelog() {
		assertException(MojoFailureException.class, "forget to update the changelog");
	}

	private File getFile(String filename) throws URISyntaxException {
		URL resource = this.getClass().getClassLoader().getResource(filename);
		return resource != null ? new File(resource.toURI()) : null;
	}

	private void assertException(Class<? extends AbstractMojoExecutionException> ex, String message) {
		try {
			sut.execute();
			fail("An exception was excepted but was not thrown");
		} catch (Exception e) {
			assertThat(e).isInstanceOf(ex);
			assertThat(e).hasMessageContaining(message);
		}
	}
}
