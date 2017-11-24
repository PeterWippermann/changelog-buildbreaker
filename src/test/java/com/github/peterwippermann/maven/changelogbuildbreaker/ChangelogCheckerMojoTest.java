package com.github.peterwippermann.maven.changelogbuildbreaker;

import org.apache.maven.plugin.MojoFailureException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import static com.github.peterwippermann.maven.changelogbuildbreaker.ChangelogCheckerMojo.DEFAULT_PATTERN_FOR_UNRELEASED_CHANGES;
import static org.assertj.core.api.Assertions.assertThat;

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

        // TODO this would be prettier with Java 8:
        // assertThatThrownBy(() -> sut.execute()).isInstanceOf(MojoFailureException.class)
        //        .hasMessageContaining("forget to update the changelog");
        try {
            sut.execute();
        } catch (MojoFailureException e) {
            assertThat(e).hasMessageContaining("forget to update the changelog");
        }
    }

    @Test
    public void testExecute_nonEmptyUnreleased_noStartingBlankLine() throws Exception {
        sut.setChangelogFile(getFile("CHANGELOG-non-empty-unreleased-missing-starting-new-line.MD"));

        // FIXME this should fail exactly as in testExecute_nonEmptyUnreleased
        sut.execute();
    }

    @Test
    public void testExecute_nonEmptyUnreleased_noEndingBlankLine() throws Exception {
        sut.setChangelogFile(getFile("CHANGELOG-non-empty-unreleased-missing-ending-new-line.MD"));

        // FIXME this should fail exactly as in testExecute_nonEmptyUnreleased
        sut.execute();
    }

    private File getFile(String filename) throws URISyntaxException {
        URL resource = this.getClass().getClassLoader().getResource(filename);
        return resource != null ? new File(resource.toURI()) : null;
    }
}
